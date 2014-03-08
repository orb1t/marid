/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.logging;

import org.marid.concurrent.MaridTimerTask;
import org.marid.io.FastArrayOutputStream;
import org.marid.logging.formatters.DefaultFormatter;
import org.marid.logging.monitoring.CompressedLogRecords;
import org.marid.logging.monitoring.LogMXBean;
import org.marid.logging.monitoring.LogRecordsArray;
import org.marid.management.MaridNotificationEmitter;
import org.marid.xml.bind.JaxbUtil;
import org.marid.xml.bind.XmlLogRecord;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import static java.time.Instant.ofEpochMilli;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.ErrorManager.*;
import static org.marid.util.Utils.ZONE_ID;

/**
 * @author Dmitry Ovchinnikov
 */
public class FsHandler extends AbstractHandler implements LogMXBean, MaridNotificationEmitter {

    protected final ThreadLocal<AtomicLong> threadNotificationCounter = ThreadLocal.withInitial(AtomicLong::new);
    protected final File directory;
    protected final int depth;
    protected final boolean formatted;
    protected final boolean compressedNotifications;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Timer timer = new Timer(getClass().getName(), true);
    protected final ThreadGroup group = new ThreadGroup(getClass().getSimpleName() + System.identityHashCode(this));
    protected final int poolSize;
    protected volatile OutputStream outputStream;
    protected volatile File file;
    protected volatile boolean dirty;

    public FsHandler() throws Exception {
        final LogManager logManager = LogManager.getLogManager();
        final String prefix = getClass().getCanonicalName();
        final String dir = logManager.getProperty(prefix + ".directory");
        directory = dir != null ? new File(dir) : new File(System.getProperty("user.home"), "marid-logs");
        final String depthStr = logManager.getProperty(prefix + ".depth");
        depth = depthStr != null ? Integer.parseInt(depthStr) : 7;
        formatted = "true".equalsIgnoreCase(logManager.getProperty(prefix + ".formatted"));
        compressedNotifications = "true".equalsIgnoreCase(logManager.getProperty(prefix + ".compressedNotifications"));
        final String flushIntervalStr = logManager.getProperty(prefix + ".flushInterval");
        final long flushInterval = flushIntervalStr == null ? SECONDS.toMillis(1L) : Long.parseLong(flushIntervalStr);
        final String depthIntervalStr = logManager.getProperty(prefix + ".depthCheckInterval");
        final long depthInterval = depthIntervalStr == null ? HOURS.toMillis(1L) : Long.parseLong(depthIntervalStr);
        timer.schedule(new MaridTimerTask(this::flush), flushInterval, flushInterval);
        timer.schedule(new MaridTimerTask(this::cleanUp), depthInterval, depthInterval);
        final String poolSizeStr = logManager.getProperty(prefix + ".poolSize");
        poolSize = poolSizeStr == null ? 4 : Integer.parseInt(poolSizeStr);
    }

    protected String getExt() {
        return ".log";
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record) || record.getSourceClassName() != null) {
            return;
        }
        final String name = ofEpochMilli(record.getMillis()).atZone(ZONE_ID).toLocalDate().toString();
        lock.writeLock().lock();
        try {
            if (file != null) {
                if (!file.getName().startsWith(name)) {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (Exception x) {
                        getErrorManager().error("Unable to close stream", x, CLOSE_FAILURE);
                    } finally {
                        file = null;
                    }
                }
            }
            if (file == null) {
                if (directory.mkdirs()) {
                    assert directory.exists();
                }
                file = new File(directory, name + getExt());
                outputStream = newOutputStream(file);
            }
            final FastArrayOutputStream faos = new FastArrayOutputStream(1024);
            JaxbUtil.save(new XmlLogRecord(record), faos, true, formatted);
            final ByteBuffer trimmedBuffer = faos.getTrimmedByteBuffer();
            if (faos.size() - trimmedBuffer.limit() >= 2) {
                trimmedBuffer.array()[trimmedBuffer.limit() - 2] = '\n';
                trimmedBuffer.array()[trimmedBuffer.limit() - 1] = '\f';
                outputStream.write(trimmedBuffer.array(), trimmedBuffer.position(), trimmedBuffer.remaining() + 2);
            } else {
                outputStream.write(trimmedBuffer.array(), trimmedBuffer.position(), trimmedBuffer.remaining());
                outputStream.write('\n');
                outputStream.write('\f');
            }
            dirty = true;
        } catch (Exception x) {
            getErrorManager().error("Unable to write", x, WRITE_FAILURE);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void cleanUp() {
        try {
            final File[] files = directory.listFiles((dir, name) -> name.matches("\\d{4}-\\d{2}-\\d{2}.+"));
            if (files == null) {
                getErrorManager().error("Unable to clean-up", null, GENERIC_FAILURE);
                return;
            }
            Arrays.sort(files);
            if (depth > 0 && files.length > depth) {
                for (final File f : Arrays.copyOf(files, files.length - depth)) {
                    if (f.equals(file)) {
                        lock.writeLock().lock();
                        try {
                            if (f.equals(file)) {
                                break;
                            }
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                    if (!f.delete()) {
                        getErrorManager().error("Unable to delete " + f, null, GENERIC_FAILURE);
                    }
                }
            }
        } catch (Exception x) {
            getErrorManager().error("Unable to clean-up", x, GENERIC_FAILURE);
        }
    }

    protected void flushDirect() throws IOException {
        outputStream.flush();
        ((FileOutputStream) outputStream).getFD().sync();
    }

    protected InputStream newInputStream(File file) throws IOException {
        return new FileInputStream(file);
    }

    protected OutputStream newOutputStream(File file) throws IOException {
        return new FileOutputStream(file);
    }

    @Override
    public void flush() {
        if (file != null & dirty) {
            lock.writeLock().lock();
            try {
                if (file != null & dirty) {
                    flushDirect();
                }
            } catch (Exception x) {
                getErrorManager().error("Unable to flush " + file, x, FLUSH_FAILURE);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void close() throws SecurityException {
        timer.cancel();
        lock.writeLock().lock();
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception x) {
            getErrorManager().error("Unable to close " + file, x, CLOSE_FAILURE);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private long query(long startTime, long stopTime, String lev, String ln, String msg, Consumer<LogRecord> c) {
        if (group.activeCount() > poolSize) {
            return -1L;
        } else {
            final Pattern lnp = ln == null || ln.isEmpty() ? null : Pattern.compile(ln);
            final Pattern msgp = msg == null || msg.isEmpty() ? null : Pattern.compile(msg);
            final Level l = Level.parse(lev);
            final File startFile = new File(directory, ofEpochMilli(startTime).atZone(ZONE_ID).toLocalDate() + getExt());
            final File stopFile = new File(directory, ofEpochMilli(stopTime).atZone(ZONE_ID).toLocalDate() + getExt());
            final File[] files = directory.listFiles(f ->
                    f.getName().endsWith(getExt()) && f.compareTo(startFile) >= 0 && f.compareTo(stopFile) <= 0);
            Arrays.sort(files);
            final Thread thread = new Thread(group, () -> {
                FileLoop:
                for (final File f : files) {
                    try (final Scanner scanner = new Scanner(newInputStream(f), "UTF-8").useDelimiter("\\f")) {
                        while (scanner.hasNext()) {
                            final XmlLogRecord r = JaxbUtil.load(XmlLogRecord.class, new StringReader(scanner.next()));
                            final long millis = r.getMillis();
                            if (millis >= stopTime) {
                                break FileLoop;
                            } else if (millis < startTime) {
                                continue;
                            }
                            if (r.getLoggerName() != null && lnp != null && !lnp.matcher(r.getLoggerName()).matches()) {
                                continue;
                            }
                            if (msgp != null && !msgp.matcher(r.getMessage()).matches()) {
                                continue;
                            }
                            final LogRecord lr = r.toLogRecord();
                            if (lr.getLevel().intValue() <= l.intValue()) {
                                continue;
                            }
                            c.accept(lr);
                        }
                    } catch (Exception x) {
                        getErrorManager().error("Query processing error", x, GENERIC_FAILURE);
                    }
                }
            });
            thread.setName(Long.toString(thread.getId()));
            thread.setDaemon(true);
            thread.start();
            return thread.getId();
        }
    }

    @Override
    public long query(long startTime, long stopTime, String level, String loggerName, String message) {
        return query(startTime, stopTime, level, loggerName, message, r -> {
            final Notification notification = new Notification(
                    "log.record",
                    Thread.currentThread().getId(),
                    threadNotificationCounter.get().getAndIncrement(),
                    System.currentTimeMillis(),
                    new DefaultFormatter().format(r));
            notification.setUserData(r);
            Emitter.sendNotification(this, notification, Exception::hashCode);
        });
    }

    @Override
    public long bulkQuery(long startTime, long stopTime, int bulkSize, String level, String loggerName, String message) {
        final List<LogRecord> logRecords = new ArrayList<>(bulkSize);
        return query(startTime, stopTime, level, loggerName, message, r -> {
            logRecords.add(r);
            if (logRecords.size() >= bulkSize) {
                final Notification notification = new Notification(
                        "log.records",
                        Thread.currentThread().getId(),
                        threadNotificationCounter.get().getAndIncrement(),
                        System.currentTimeMillis());
                notification.setUserData(compressedNotifications
                        ? new CompressedLogRecords(logRecords)
                        : new LogRecordsArray(logRecords));
                Emitter.sendNotification(this, notification, Exception::hashCode);
                logRecords.clear();
            }
        });
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return MBEAN_NOTIFICATION_INFOS;
    }
}
