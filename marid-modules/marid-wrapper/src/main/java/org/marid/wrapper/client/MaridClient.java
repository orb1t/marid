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

package org.marid.wrapper.client;

import org.marid.io.GzipInputStream;
import org.marid.io.GzipOutputStream;
import org.marid.net.SocketWrapper;
import org.marid.wrapper.WrapperConstants;
import org.marid.wrapper.data.DeployConf;
import org.marid.wrapper.data.ListenLogsRequest;
import org.marid.wrapper.data.UploadRequest;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.server.UID;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridClient extends SocketWrapper<SSLSocket, ObjectInputStream, ObjectOutputStream> implements Runnable {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Thread thread;
    private final ConcurrentMap<UID, Handler> logHandlers = new ConcurrentHashMap<>();
    private final ConcurrentMap<UID, EventListener> eventListeners = new ConcurrentHashMap<>();
    private final int mode;
    private final AtomicReference<Throwable> error = new AtomicReference<>();

    public MaridClient(MaridClientSettings settings) throws IOException {
        super(settings.getSecureProfile().getSocketFactory(), settings.socketConfigurer());
        this.mode = settings.getMode();
        thread = new Thread(this, getClass().getSimpleName() + "(" + socket + ")");
        thread.start();
    }

    @Override
    protected ObjectInputStream getInput(InputStream inputStream) throws IOException {
        switch (mode) {
            case WrapperConstants.MODE_DEFAULT:
                return new ObjectInputStream(inputStream);
            case WrapperConstants.MODE_GZIP:
                return new ObjectInputStream(new GzipInputStream(inputStream, 8192));
            default:
                throw new IllegalStateException("Invalid mode: " + mode);
        }
    }

    @Override
    protected ObjectOutputStream getOutput(OutputStream outputStream) throws IOException {
        switch (mode) {
            case WrapperConstants.MODE_DEFAULT:
                return new ObjectOutputStream(outputStream);
            case WrapperConstants.MODE_GZIP:
                return new ObjectOutputStream(new GzipOutputStream(outputStream, 8192, true));
            default:
                throw new IllegalStateException("Invalid mode: " + mode);
        }
    }

    private UID write(Object object) throws IOException {
        final Throwable throwable = error.get();
        if (throwable != null) {
            throw throwable instanceof IOException ? (IOException) throwable : new IOException(throwable);
        }
        lock.writeLock().lock();
        try {
            final UID uid = new UID();
            uid.write(output);
            output.writeObject(object);
            output.flush();
            return uid;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addLogHandler(Handler handler, Level level) throws IOException {
        logHandlers.put(write(new ListenLogsRequest(level)), handler);
    }

    public void upload(DeployConf deployConf, UploadListener uploadListener) throws IOException {
        eventListeners.put(write(new UploadRequest(deployConf)), uploadListener);
    }

    void with(final UploadListener listener, String cmd) throws Exception {
        switch (cmd) {
            case "WAIT_LOCK":
                listener.onWaitLock();
                break;
            case "WAIT_DATA":
                listener.onWaitData();
                final Path path = listener.getPath();
                final long size = Files.size(path);
                listener.onWriteData(0L, size);
                Files.copy(path, new OutputStream() {

                    private long count = 0L;

                    @Override
                    public void write(int b) throws IOException {
                        output.write(b);
                        count++;
                        listener.onWriteData(count, size);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        output.write(b, off, len);
                        count += len;
                        listener.onWriteData(count, size);
                    }
                });
                break;
            case "WAIT_LOCK_FAILED":
                listener.onWaitLockFailed();
                break;
            case "DESTROY_PROCESS":
                listener.onDestroyProcess();
                break;
            case "COPY_DATA":
                listener.onCopyData();
                break;
            case "EXTRACT_DATA":
                listener.onExtractData();
                break;
            case "OK":
                listener.onOk();
                break;
            case "ACCESS_DENIED":
                listener.onAccessDenied();
                break;
            case "ERROR":
                listener.onError((Throwable) input.readObject());
                break;
            case "WRITE_CONF":
                listener.onWriteConf();
                break;
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            lock.readLock().lock();
            try {
                final UID uid = UID.read(input);
                if (logHandlers.containsKey(uid)) {
                    final Handler handler = logHandlers.get(uid);
                    final LogRecord[] logRecords = (LogRecord[]) input.readObject();
                    for (final LogRecord logRecord : logRecords) {
                        handler.publish(logRecord);
                    }
                } else {
                    final EventListener listener = eventListeners.get(uid);
                    final Object cmd = input.readObject();
                    final Method method = getClass().getDeclaredMethod("with", listener.getClass(), cmd.getClass());
                    method.invoke(this, listener, cmd);
                }
            } catch (Exception x) {
                if (socket.isClosed()) {
                    break;
                } else {
                    try {
                        socket.close();
                    } catch (Exception y) {
                        x.addSuppressed(y);
                    }
                    error.set(x);
                }
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    public interface UploadListener extends EventListener {

        Path getPath();

        void onWaitLock();

        void onWaitLockFailed();

        void onWaitData();

        void onWriteData(long count, long size);

        void onDestroyProcess();

        void onCopyData();

        void onExtractData();

        void onWriteConf();

        void onAccessDenied();

        void onOk();

        void onError(Throwable cause);
    }
}
