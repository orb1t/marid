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

import java.io.*;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.marid.wrapper.Log.info;
import static org.marid.wrapper.Log.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Session implements Callable<Session> {

    private final Socket socket;
    private final String sessionId;
    private final Logger log;

    public Session(Socket socket) {
        this.socket = socket;
        this.sessionId = UUID.nameUUIDFromBytes(socket.toString().getBytes(ISO_8859_1)).toString();
        this.log = Logger.getLogger(toString());
    }

    private Path bak() throws Exception {
        if (Wrapper.CUR_FILE.isFile()) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss'.zip'");
            final File file = new File(Wrapper.BACKUPS, dateFormat.format(new Date()));
            final Path source = Wrapper.CUR_FILE.toPath();
            final Path target = file.toPath();
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target;
        } else {
            return null;
        }
    }

    private void restore(Path path) throws Exception {
        if (path == null) {
            return;
        }
        info(log, "Restoring {0}", path);
        Files.move(path, Wrapper.CUR_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void upload(final ClientContext clientContext, Properties properties) throws Exception {
        Path path = null;
        try {
            path = bak();
            final long size = Long.parseLong(properties.getProperty("size"));
            try (final FileChannel fc = FileChannel.open(Wrapper.CUR_FILE.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                fc.transferFrom(Channels.newChannel(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        int n = clientContext.inputStream.read();
                        clientContext.outputStream.writeObject(n);
                        return n;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        int n = clientContext.inputStream.read(b, off, len);
                        clientContext.outputStream.writeObject(n);
                        return n;
                    }
                }), 0L, size);
            }
            clientContext.outputStream.writeObject(true);
        } catch (Exception x) {
            restore(path);
            throw x;
        } finally {
            extract();
        }
    }

    private void extract() throws Exception {
        FileUtils.removeDir(Wrapper.TARGET.toPath());
        try (FileSystem zipfs = FileSystems.getFileSystem(Wrapper.CUR_FILE.toURI())) {
            for (Path src : zipfs.getRootDirectories()) {
                FileUtils.copyDir(src, Wrapper.TARGET.toPath());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Session call() throws Exception {
        try (final ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
             final ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream())) {
            final ClientContext clientContext = new ClientContext(
                    (Map<String, String>) is.readObject(), (Properties) is.readObject(), is, os);
            os.writeObject(new TreeMap<>(System.getenv()));
            os.writeObject(System.getProperties());
            LOOP: while (true) {
                final Properties properties = (Properties) is.readObject();
                switch (properties.getProperty("cmd", "exit")) {
                    case "upload":
                        synchronized (Wrapper.class) {
                            Wrapper.stop();
                            upload(clientContext, properties);
                            Wrapper.run();
                        }
                        break;
                    case "exit":
                        break LOOP;
                }
            }
        } catch (Exception x) {
            warning(log, "Session error", x);
        }
        return this;
    }

    public Logger getLog() {
        return log;
    }

    @Override
    public final String toString() {
        return sessionId + "(" + socket + ")";
    }

    private class ClientContext {

        private final Map<String, String> env;
        private final Properties props;
        private final ObjectInputStream inputStream;
        private final ObjectOutputStream outputStream;

        public ClientContext(
                Map<String, String> env,
                Properties props,
                ObjectInputStream inputStream,
                ObjectOutputStream outputStream) {
            this.env = env;
            this.props = props;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }
    }
}
