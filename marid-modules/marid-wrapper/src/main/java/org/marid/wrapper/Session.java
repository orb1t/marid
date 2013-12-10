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
import org.marid.wrapper.data.AuthResponse;
import org.marid.wrapper.data.ClientData;
import org.marid.wrapper.data.DeployConf;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.server.UID;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.marid.wrapper.Log.*;
import static org.marid.wrapper.Wrapper.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Session implements Runnable {

    private static final Logger LOG = Logger.getLogger(Session.class.getName());

    final SSLSocket socket;
    final SSLSession session;
    final Set<String> roles = new HashSet<>();
    private RequestContext logHandler;
    static final SimpleDateFormat APP_BACKUP_FORMAT = new SimpleDateFormat("APP-yyyy-MM-dd-HH-mm-ss'.zip'");
    static final SimpleDateFormat RES_BACKUP_FORMAT = new SimpleDateFormat("RES-yyyy-MM-dd-HH-mm-ss'.zip'");

    public Session(SSLSocket socket, SSLSession session) throws IOException {
        this.socket = socket;
        this.session = session;
    }

    private void checkClientData(ObjectInputStream is, ObjectOutputStream os) throws Exception {
        final ClientData clientData = (ClientData) is.readObject();
        info(LOG, "{0} {1}", socket, clientData.toString());
        final KeyStore keyStore = SECURE_PROFILE.getKeyStore();
        final String user = clientData.getUser();
        try {
            keyStore.getKey(user, clientData.password());
        } catch (UnrecoverableKeyException x) {
            os.writeObject(new AuthResponse("accessDenied"));
            throw new IllegalAccessException("Access denied for " + user);
        }
        roles.addAll(Arrays.asList(USERS.getProperty("MW_ROLES_" + user, "admin").split(",")));
        final AuthResponse response = new AuthResponse("ok", roles);
        os.writeObject(response);
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
        final DeployConf deployConf = JaxbStreams.read(DeployConf.class, ctx.in);
        ctx.sendResponse(Response.WAIT_DATA);
        while (true) {
            ctx.sendResponse(Response.WAIT_LOCK);
            if (Wrapper.processLock.tryLock(1L, TimeUnit.MINUTES)) {
                try {
                    final Path curZip = BACKUPS.resolve("cur.zip");
                    Files.copy(new LimitedInputStream(ctx.in, ctx.in.readLong()), curZip, REPLACE_EXISTING);
                    Wrapper.destroyProcess();
                    if (Files.isDirectory(TARGET)) {
                        final Path res = BACKUPS.resolve(RES_BACKUP_FORMAT.format(new Date()));
                        info(LOG, "{0} Copying resources", socket);
                        FileUtils.toZipMove(res, LOGS, TARGET.resolve("ext"), TARGET.resolve("configuration.xml"));
                        info(LOG, "{0} Copying application files", socket);
                        FileUtils.moveToZip(TARGET, BACKUPS.resolve(APP_BACKUP_FORMAT.format(new Date())));
                    }
                    info(LOG, "{0} Extracting data", socket);
                    FileUtils.copyFromZip(curZip, TARGET);
                    info(LOG, "{0} Writing configuration", socket);
                    JaxbStreams.writeXml(TARGET.resolve("configuration.xml"), deployConf);
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
    }

    void listenLogs(final RequestContext ctx) throws Exception {
        try (final RequestContext context = logHandler) {
            Logger.getLogger("").removeHandler(context);
        }
        Logger.getLogger("").addHandler(logHandler = ctx);
        new Thread(logHandler).start();
    }

    private void doRun(ObjectInputStream is, ObjectOutputStream os) throws Exception {
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
        try (final ObjectInputStream i = new ObjectInputStream(socket.getInputStream());
             final ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream())) {
            doRun(i, o);
        } catch (Exception x) {
            warning(LOG, "{0} session error", x, this);
        } finally {
            try (final RequestContext context = logHandler; final Socket s = socket) {
                Logger.getLogger("").removeHandler(context);
                info(LOG, "{0} Closed", s);
            } catch (Exception x) {
                warning(LOG, "{0} Unable to close", x, socket);
            }
        }
    }

    @Override
    public String toString() {
        return socket.toString();
    }

    private class RequestContext extends Handler implements AutoCloseable, Runnable {

        private final UID id;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private final BlockingQueue<LogRecord> records = new LinkedBlockingQueue<>();

        private RequestContext(UID id, ObjectInputStream in, ObjectOutputStream out) {
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
                        out.writeObject(p);
                    } else if (p instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) p).writeTo(out);
                    } else {
                        throw new IllegalArgumentException("Invalid parameter: " + p);
                    }
                }
            }
        }

        @Override
        public void publish(LogRecord record) {
            try {
                records.add(record);
            } catch (Exception x) {
                // nop
            }
        }

        @Override
        public void flush() {
            final int size = records.size();
            final List<LogRecord> recordList = new ArrayList<>(size);
            records.drainTo(recordList, size);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final ObjectOutputStream os = new ObjectOutputStream(new GzipOutputStream(bos, 16384, false))) {
                os.writeObject(recordList);
            } catch (Exception x) {
                return;
            }
            try {
                sendResponse(Response.LOG_RECORDS, bos.size(), bos);
            } catch (Exception x) {
                // We cannot log here
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000L);
                    if (getLevel().equals(Level.OFF)) {
                        break;
                    }
                    flush();
                } catch (InterruptedException x) {
                    break;
                }
            }
        }

        @Override
        public void close() throws SecurityException {
            setLevel(Level.OFF);
            flush();
        }
    }
}
