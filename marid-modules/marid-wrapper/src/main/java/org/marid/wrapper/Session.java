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

import org.marid.io.GzipInputStream;
import org.marid.io.GzipOutputStream;
import org.marid.io.JaxbStreams;
import org.marid.io.LimitedInputStream;
import org.marid.nio.FileUtils;
import org.marid.secure.SecureProfile;
import org.marid.wrapper.data.*;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import static org.marid.methods.LogMethods.*;
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
        final KeyStore keyStore = SecureProfile.DEFAULT.getKeyStore();
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

    @Roles({"admin", "upload"})
    void process(RequestContext ctx, UploadRequest uploadRequest) throws Exception {
        while (true) {
            ctx.write("WAIT_LOCK");
            if (Wrapper.processLock.tryLock(1L, TimeUnit.MINUTES)) {
                ctx.write("WAIT_DATA");
                try {
                    final Path curZip = BACKUPS.resolve("cur.zip");
                    info(LOG, "{0} Receiving data", socket);
                    Files.copy(new LimitedInputStream(ctx.in, ctx.in.readLong()), curZip, REPLACE_EXISTING);
                    ctx.write("DESTROY_PROCESS");
                    Wrapper.destroyProcess();
                    ctx.write("COPY_DATA");
                    if (Files.isDirectory(TARGET)) {
                        final Path res = BACKUPS.resolve(RES_BACKUP_FORMAT.format(new Date()));
                        info(LOG, "{0} Copying resources", socket);
                        FileUtils.toZipMove(res, LOGS, TARGET.resolve("ext"), TARGET.resolve("configuration.xml"));
                        info(LOG, "{0} Copying application files", socket);
                        FileUtils.moveToZip(TARGET, BACKUPS.resolve(APP_BACKUP_FORMAT.format(new Date())));
                    }
                    ctx.write("EXTRACT_DATA");
                    info(LOG, "{0} Extracting data", socket);
                    FileUtils.copyFromZip(curZip, TARGET);
                    ctx.write("WRITE_CONF");
                    info(LOG, "{0} Writing configuration", socket);
                    JaxbStreams.writeXml(TARGET.resolve("configuration.xml"), uploadRequest.getDeployConf());
                    break;
                } finally {
                    Wrapper.processLock.unlock();
                }
            } else {
                ctx.write("WAIT_LOCK_FAILED");
                final Object request = ctx.in.readObject();
                switch (request.toString()) {
                    case "CONTINUE":
                        continue;
                    case "CLOSE":
                        return;
                    default:
                        throw new IllegalStateException("Illegal request: " + request);
                }
            }
        }
    }

    @Roles({"admin", "listenLogs"})
    void process(RequestContext ctx, ListenLogsRequest listenLogsRequest) throws Exception {
        ctx.setLevel(listenLogsRequest.getLevel());
        try (final RequestContext context = logHandler) {
            Logger.getLogger("").removeHandler(context);
        }
        Logger.getLogger("").addHandler(logHandler = ctx);
        new Thread(logHandler).start();
    }

    private void doRun(ObjectInputStream is, ObjectOutputStream os) throws Exception {
        checkClientData(is, os);
        while (true) {
            final UID uid = UID.read(is);
            final RequestContext context = new RequestContext(uid, is, os);
            final Object request = context.in.readObject();
            fine(LOG, "{0} Command {1} received", socket, request);
            try {
                final Method method = getClass().getDeclaredMethod("process", RequestContext.class, request.getClass());
                final Set<String> roles = new HashSet<>(Arrays.asList(method.getAnnotation(Roles.class).value()));
                roles.retainAll(this.roles);
                if (!roles.isEmpty()) {
                    method.invoke(this, context, request);
                    context.write("OK");
                } else {
                    context.write("ACCESS_DENIED");
                }
            } catch (NoSuchMethodException x) {
                context.write("UNKNOWN_REQUEST");
            } catch (SecurityException | IllegalAccessException x) {
                context.write("SECURITY_ERROR");
            } catch (ClosedSessionException x) {
                break;
            } catch (InvocationTargetException x) {
                context.write("ERROR");
                context.write(x.getCause());
            }
        }
    }

    @Override
    public void run() {
        try (final InputStream inp = socket.getInputStream();
             final OutputStream out = socket.getOutputStream()) {
            final int code = inp.read();
            switch (code) {
                case 0:
                    try (final ObjectInputStream i = new ObjectInputStream(inp);
                         final ObjectOutputStream o = new ObjectOutputStream(out)) {
                        doRun(i, o);
                    }
                    break;
                case 1:
                    try (final ObjectInputStream i = new ObjectInputStream(new GzipInputStream(inp, 8192));
                         final ObjectOutputStream o = new ObjectOutputStream(new GzipOutputStream(out, 8192, true))) {
                        doRun(i, o);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid code: " + code);
            }
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
            try {
                write(recordList.toArray(new LogRecord[recordList.size()]));
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

        public void write(Object object) throws IOException {
            synchronized (Session.this) {
                id.write(out);
                out.writeObject(object);
                out.flush();
            }
        }
    }
}
