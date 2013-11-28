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

import org.marid.io.JaxbStreams;
import org.marid.io.LimitedInputStream;
import org.marid.nio.FileUtils;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.rmi.server.UID;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
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
    private final Map<UID, Handler> logHandlerMap = new HashMap<>();
    static final SimpleDateFormat backupFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss'.zip'");

    public Session(SSLSocket socket, SSLSession session) {
        this.socket = socket;
        this.session = session;
    }

    private Set<String> checkClientData(DataInputStream is, DataOutputStream os) throws Exception {
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
        final String[] roles = Wrapper.USERS.getProperty("MW_ROLES_" + user, "admin").split(",");
        final AuthResponse response = new AuthResponse("ok", roles);
        JaxbStreams.write(JAXB_CONTEXT, os, response);
        info(LOG, "{0} User {1} was authorized as {2}", socket, user, response.getRoles());
        return response.getRoles();
    }

    void upload(Set<String> roles, UID uid, DataInputStream is, DataOutputStream os) throws Exception {
        if (!roles.contains("admin") && !roles.contains("uploader")) {
            synchronized (this) {
                uid.write(os);
                os.writeByte(Response.ACCESS_DENIED);
            }
        }
        try {
            while (true) {
                synchronized (this) {
                    uid.write(os);
                    os.writeByte(Response.WAIT_LOCK);
                }
                if (Wrapper.processLock.tryLock(1L, TimeUnit.MINUTES)) {
                    try {
                        final Path curZip = Wrapper.BACKUPS.resolve("cur.zip");
                        if (Files.isRegularFile(curZip)) {
                            final Path backupZip = Wrapper.BACKUPS.resolve(backupFormat.format(new Date()));
                            Files.move(curZip, backupZip, StandardCopyOption.REPLACE_EXISTING);
                        }
                        Files.copy(new LimitedInputStream(is, is.readLong()), curZip);
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
                    synchronized (this) {
                        uid.write(os);
                        os.writeByte(Response.WAIT_LOCK_FAILED);
                    }
                    switch (is.readByte()) {
                        case Request.CONTINUE:
                            continue;
                        case Request.CLOSE:
                            synchronized (this) {
                                uid.write(os);
                                os.writeByte(Response.BREAK);
                            }
                            return;
                    }
                }
            }
            synchronized (this) {
                uid.write(os);
                os.writeByte(Response.OK);
            }
        } catch (Exception x) {
            synchronized (this) {
                uid.write(os);
                os.writeByte(Response.EXCEPTION);
                os.writeUTF(x.toString());
            }
        }
    }

    void listenLogs(Set<String> roles, UID uid, DataInputStream is, DataOutputStream os) throws Exception {

    }

    private void doRun(DataInputStream is, DataOutputStream os) throws Exception {
        final Set<String> roles = checkClientData(is, os);
        while (true) {
            final byte request = is.readByte();
            final UID uid = UID.read(is);
            fine(LOG, "{0} Command {1} received", socket, request);
            switch (request) {
                case Request.UPLOAD:
                    upload(roles, uid, is, os);
                    break;
                case Request.LISTEN_LOGS:
                    listenLogs(roles, uid, is, os);
                    break;
                case Request.CLOSE:
                    synchronized (this) {
                        uid.write(os);
                        os.writeBoolean(true);
                    }
                    return;
                default:
                    synchronized (this) {
                        uid.write(os);
                        os.writeUTF("unknown");
                    }
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
            final Logger rootLogger = Logger.getLogger("");
            for (final Handler handler : logHandlerMap.values()) {
                rootLogger.removeHandler(handler);
            }
            logHandlerMap.clear();
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
}
