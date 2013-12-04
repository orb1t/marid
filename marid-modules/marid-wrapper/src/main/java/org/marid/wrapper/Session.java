/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.wrapper;

import org.marid.io.GzipOutputStream;
import org.marid.io.JaxbStreams;
import org.marid.io.LimitedInputStream;
import org.marid.nio.FileUtils;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.server.UID;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Session implements Runnable {

    private static final Logger LOG = Logger.getLogger(Session.class.getName());
    static final JAXBContext JAXB_CONTEXT = ParseUtils.getJaxbContext(ClientData.class, AuthResponse.class);

    final SSLSocket socket;
    final SSLSession session;
    final Set<String> roles = new HashSet<>();
    private Handler logHandler;
    static final SimpleDateFormat backupFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss'.zip'");

    public Session(SSLSocket socket, SSLSession session) throws IOException {
        this.socket = socket;
        this.session = session;
    }

    private void checkClientData(DataInputStream is, DataOutputStream os) throws Exception {
        final byte[] data = new byte[is.readInt()];
        is.readFully(data);
        final Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        final ClientData clientData = (ClientData) unmarshaller.unmarshal(new ByteArrayInputStream(data));
        info(LOG, "{0} {1}", socket, clientData.toString());
        final KeyStore keyStore = SecureContext.getKeyStore();
        final String user = clientData.getUser();
        try {
            keyStore.getKey(user, clientData.getPassword().toCharArray());
        } catch (UnrecoverableKeyException x) {
            JaxbStreams.write(JAXB_CONTEXT, os, new AuthResponse("accessDenied"));
            throw new IllegalAccessException("Access denied for " + user);
        }
        roles.addAll(Arrays.asList(Wrapper.USERS.getProperty("MW_ROLES_" + user, "admin").split(",")));
        final AuthResponse response = new AuthResponse("ok", roles);
        JaxbStreams.write(JAXB_CONTEXT, os, response);
        info(LOG, "{0} User {1} was authorized as {2}", socket, user, response.getRoles());
    }

    void process(RequestContext context, String method, String... roles) throws Exception {
        final Set<String> roleSet = new HashSet<>(Arrays.asList(roles));
        roleSet.retainAll(this.roles);
        if (roleSet.isEmpty()) {
            context.sendResponse(Response.ACCESS_DENIED);
        } else {
            try {
                getClass().getDeclaredMethod(method, RequestContext.class).invoke(this, context);
                context.sendResponse(Response.OK);
            } catch (Exception x) {
                context.sendResponse(Response.EXCEPTION, x.toString());
            }
        }
    }

    void upload(RequestContext ctx) throws Exception {
        while (true) {
            ctx.sendResponse(Response.WAIT_LOCK);
            if (Wrapper.processLock.tryLock(1L, TimeUnit.MINUTES)) {
                try {
                    final Path curZip = Wrapper.BACKUPS.resolve("cur.zip");
                    if (Files.isRegularFile(curZip)) {
                        final Path backupZip = Wrapper.BACKUPS.resolve(backupFormat.format(new Date()));
                        Files.move(curZip, backupZip, StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.copy(new LimitedInputStream(ctx.in, ctx.in.readLong()), curZip);
                    Wrapper.destroyProcess();
                    if (Files.isDirectory(Wrapper.TARGET)) {
                        Files.walkFileTree(Wrapper.TARGET, FileUtils.RECURSIVE_CLEANER);
                    }
                    FileUtils.copyFromZip(curZip, Wrapper.TARGET);
                    break;
                } finally {
                    Wrapper.processLock.unlock();
                }
            } else {
                ctx.sendResponse(Response.WAIT_LOCK_FAILED);
                switch (ctx.in.readByte()) {
                    case Request.CONTINUE:
                        continue;
                    case Request.CLOSE:
                        ctx.sendResponse(Response.BREAK);
                        return;
                }
            }
        }
        ctx.sendResponse(Response.OK);
    }

    void listenLogs(final RequestContext ctx) throws Exception {
        if (logHandler != null) {
            Logger.getLogger("").removeHandler(logHandler);
        }
        Logger.getLogger("").addHandler(logHandler = ctx);
        ctx.sendResponse(Response.OK);
    }

    private void doRun(DataInputStream is, DataOutputStream os) throws Exception {
        checkClientData(is, os);
        while (true) {
            final byte request = is.readByte();
            final UID uid = UID.read(is);
            final RequestContext context = new RequestContext(uid, is, os);
            fine(LOG, "{0} Command {1} received", socket, request);
            switch (request) {
                case Request.UPLOAD:
                    process(context, "upload", "admin", "uploader");
                    break;
                case Request.LISTEN_LOGS:
                    process(context, "listenLogs", "admin", "listenLogs");
                    break;
                case Request.CLOSE:
                    context.sendResponse(Response.OK);
                    return;
                default:
                    context.sendResponse(Response.UNKNOWN_REQUEST);
                    break;
            }
        }
    }

    @Override
    public void run() {
        try (final DataInputStream i = new DataInputStream(socket.getInputStream());
             final DataOutputStream o = new DataOutputStream(socket.getOutputStream())) {
            doRun(i, o);
        } catch (Exception x) {
            warning(LOG, "{0} session error", x, this);
        } finally {
            if (logHandler != null) {
                Logger.getLogger("").removeHandler(logHandler);
            }
            try {
                socket.close();
                info(LOG, "{0} Closed", socket);
            } catch (Exception x) {
                warning(LOG, "{0} Unable to close", x, socket);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private class RequestContext extends Handler {

        private final UID id;
        private final DataInputStream in;
        private final DataOutputStream out;

        private RequestContext(UID id, DataInputStream in, DataOutputStream out) {
            this.id = id;
            this.in = in;
            this.out = out;
        }

        private void sendResponse(byte response, Object... parameters) throws IOException {
            synchronized (Session.this) {
                id.write(out);
                out.writeByte(response);
                for (final Object p : parameters) {
                    if (p instanceof String) {
                        out.writeUTF((String) p);
                    } else if (p instanceof Integer) {
                        out.writeInt((int) p);
                    } else if (p instanceof Long) {
                        out.writeLong((long) p);
                    } else if (p instanceof Byte) {
                        out.writeByte((byte) p);
                    } else if (p instanceof Boolean) {
                        out.writeBoolean((boolean) p);
                    } else if (p instanceof Short) {
                        out.writeShort((short) p);
                    } else if (p instanceof Double) {
                        out.writeDouble((double) p);
                    } else if (p instanceof Float) {
                        out.writeFloat((float) p);
                    } else if (p instanceof Serializable) {
                        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try (final GzipOutputStream gzos = new GzipOutputStream(bos, 8192, false);
                             final ObjectOutputStream oos = new ObjectOutputStream(gzos)) {
                            oos.writeObject(p);
                        }
                        bos.writeTo(out);
                    } else {
                        throw new IllegalArgumentException("Invalid parameter: " + p);
                    }
                }
            }
        }

        @Override
        public void publish(LogRecord record) {
            try {
                sendResponse(Response.LOG_RECORD, record);
            } catch (Exception x) {
                try {
                    Logger.getLogger("").removeHandler(this);
                } finally {
                    logHandler = null;
                    warning(LOG, "{0} Unable to write a log record", x, socket);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
