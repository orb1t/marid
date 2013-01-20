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
package org.marid.db.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log recordset.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class LogRecordSet implements Externalizable {

    private final ArrayList<LogRecord> logRecordList;

    /**
     * Constructs a new log record set.
     * @param records Log records.
     */
    public LogRecordSet(LogRecord... records) {
        this(Arrays.asList(records));
    }

    /**
     * Constructs a new log record set.
     * @param logRecords Log records.
     */
    public LogRecordSet(List<LogRecord> logRecords) {
        logRecordList = new ArrayList<>(logRecords);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(logRecordList.size());
        byte state = 0;
        for (LogRecord r : logRecordList) {
            out.writeLong(r.getMillis());
            out.writeLong(r.getSequenceNumber());
            out.writeInt(r.getThreadID());
            state |= r.getLevel() != null              ? 0b10000000 : 0;
            state |= r.getMessage() != null            ? 0b01000000 : 0;
            state |= r.getSourceClassName() != null    ? 0b00100000 : 0;
            state |= r.getSourceMethodName() != null   ? 0b00010000 : 0;
            state |= r.getLoggerName() != null         ? 0b00001000 : 0;
            state |= r.getResourceBundleName() != null ? 0b00000100 : 0;
            state |= r.getParameters() != null         ? 0b00000010 : 0;
            state |= r.getThrown() != null             ? 0b00000001 : 0;
            out.writeByte(state);
            if (r.getLevel() != null) {
                out.writeUTF(r.getLevel().getName());
            }
            if (r.getMessage() != null) {
                out.writeUTF(r.getMessage());
            }
            if (r.getSourceClassName() != null) {
                out.writeUTF(r.getSourceClassName());
            }
            if (r.getSourceMethodName() != null) {
                out.writeUTF(r.getSourceMethodName());
            }
            if (r.getLoggerName() != null) {
                out.writeUTF(r.getLoggerName());
            }
            if (r.getResourceBundleName() != null) {
                out.writeUTF(r.getResourceBundleName());
            }
            if (r.getParameters() != null) {
                Object[] params = r.getParameters();
                int len = params.length;
                out.writeInt(len);
                BitSet ns = new BitSet(len * 2);
                for (int i = 0; i < len; i++) {
                    ns.set(i, params[i] != null);
                    ns.set(len + i, params[i] instanceof Serializable);
                }
                byte[] nsb = ns.toByteArray();
                out.writeInt(nsb.length);
                out.write(nsb);
                for (Object p : r.getParameters()) {
                    if (p == null) {
                        continue;
                    }
                    if (!(p instanceof Serializable)) {
                        String text = p.toString();
                        out.writeInt(text.length());
                        out.writeChars(text);
                        continue;
                    }
                    try {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                            o.writeObject(p);
                        }
                        out.writeInt(b.size());
                        if (out instanceof OutputStream) {
                            b.writeTo((OutputStream)out);
                        } else {
                            out.write(b.toByteArray());
                        }
                    } catch (IOException x) {
                        throw x;
                    } catch (Exception x) {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                            o.writeObject(p.toString());
                        }
                        out.writeInt(b.size());
                        if (out instanceof OutputStream) {
                            b.writeTo((OutputStream)out);
                        } else {
                            out.write(b.toByteArray());
                        }
                    }
                }
            }
            if (r.getThrown() != null) {
                Throwable thrown = r.getThrown();
                boolean hasMessage = thrown.getMessage() != null;
                out.writeBoolean(hasMessage);
                if (hasMessage) {
                    out.writeUTF(thrown.getMessage());
                }
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            long millis = in.readLong();
            long sequenceNumber = in.readLong();
            int threadId = in.readInt();
            byte state = in.readByte();
            boolean hasLevel =                  (state & 0b10000000) > 0;
            boolean hasMessage =                (state & 0b01000000) > 0;
            boolean hasSourceClassName =        (state & 0b00100000) > 0;
            boolean hasSourceMethodName =       (state & 0b00010000) > 0;
            boolean hasLoggerName =             (state & 0b00001000) > 0;
            boolean hasResourceBundleName =     (state & 0b00000100) > 0;
            boolean hasParameters =             (state & 0b00000010) > 0;
            boolean hasThrown =                 (state & 0b00000001) > 0;
            Level level = hasLevel ? Level.parse(in.readUTF()) : Level.OFF;
            String message = hasMessage ? in.readUTF() : null;
            String sourceClassName = hasSourceClassName ? in.readUTF() : null;
            String sourceMethodName = hasSourceMethodName ? in.readUTF() : null;
            String loggerName = hasLoggerName ? in.readUTF() : null;
            String rbName = hasResourceBundleName ? in.readUTF() : null;
            Object[] params = null;
            Throwable thrown = null;
            LogRecord r = new LogRecord(level, message);
            r.setLoggerName(loggerName);
            r.setResourceBundleName(rbName);
            r.setSourceClassName(sourceClassName);
            r.setSourceMethodName(sourceMethodName);
            r.setThreadID(threadId);
            r.setSequenceNumber(sequenceNumber);
            r.setMillis(millis);
            if (hasParameters) {
                int len = in.readInt();
                byte[] nsb = new byte[in.readInt()];
                in.readFully(nsb);
                BitSet ns = BitSet.valueOf(nsb);
                params = new Object[len];
                for (int k = 0; k < len; k++) {
                    if (!ns.get(k)) {
                        continue;
                    }
                    if (ns.get(len + k)) {
                        byte[] buf = new byte[in.readInt()];
                        in.readFully(buf);
                        ByteArrayInputStream is = new ByteArrayInputStream(buf);
                        try {
                            try (ObjectInputStream ois =
                                    new ObjectInputStream(is)) {
                                params[k] = ois.readObject();
                            }
                        } catch (IOException x) {
                            throw x;
                        } catch (Exception x) {
                            params[k] = x;
                        }
                    } else {
                        char[] buf = new char[in.readInt()];
                        for (int z = 0; z < buf.length; z++) {
                            buf[z] = in.readChar();
                        }
                        params[k] = String.valueOf(buf);
                    }
                }
            }
            r.setParameters(params);
            if (hasThrown) {
                String msg = in.readBoolean() ? in.readUTF() : null;
            }
        }
    }

    private static void wt(ObjectOutput out, Throwable th) throws IOException {
        String msg = th.getMessage();
        boolean hasMessage = msg != null;
        out.writeBoolean(hasMessage);
        if (hasMessage) {
            out.writeUTF(msg);
        }
        out.writeUTF(th.getClass().getName());
        StackTraceElement[] stes = th.getStackTrace();
        out.writeInt(stes.length);
        for (StackTraceElement ste : stes) {
            out.writeInt(ste.getLineNumber());
            byte state = 0;
            state |= ste.getFileName() != null   ? 0b10000000 : 0;
            state |= ste.getClassName() != null  ? 0b01000000 : 0;
            state |= ste.getMethodName() != null ? 0b00100000 : 0;
            out.writeByte(state);
            if (ste.getFileName() != null) {
                out.writeUTF(ste.getFileName());
            }
            if (ste.getClassName() != null) {
                out.writeUTF(ste.getClassName());
            }
            if (ste.getMethodName() != null) {
                out.writeUTF(ste.getMethodName());
            }
        }
        Throwable[] suppresseds = th.getSuppressed();
        out.writeInt(suppresseds.length);
        for (Throwable suppressed : suppresseds) {
            wt(out, suppressed);
        }
        boolean hasCause = th.getCause() != null;
        out.writeBoolean(hasCause);
        if (hasCause) {
            wt(out, th.getCause());
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private static Throwable rt(ObjectInput in) throws IOException {
        String msg = null;
        boolean hasMessage = in.readBoolean();
        if (hasMessage) {
            msg = in.readUTF();
        }
        String thc = in.readUTF();
        Throwable th;
        try {
            Class<?> c;
            try {
                c = Class.forName(thc);
            } catch (Exception x) {
                c = Class.forName(thc, true,
                        Thread.currentThread().getContextClassLoader());
            }
            try {
                th = (Throwable)c.getDeclaredConstructor(
                        String.class).newInstance(msg);
            } catch (Exception x) {
                try {
                    th = (Throwable)c.getDeclaredConstructor(String.class,
                                Throwable.class).newInstance(msg, null);
                } catch (Exception y) {
                    try {
                        th = (Throwable)c.getDeclaredConstructor(
                                Throwable.class).newInstance((Throwable)null);
                    } catch (Exception z) {
                        th = (Throwable)c.newInstance();
                    }
                }
            }
        } catch (Exception x) {
            th = new UnknownException(msg, thc);
        }
        StackTraceElement[] stes = new StackTraceElement[in.readInt()];
        for (int i = 0; i < stes.length; i++) {
            int ln = in.readInt();
            byte state = in.readByte();
            boolean hasFileName =   (state & 0b10000000) > 0;
            boolean hasClassName =  (state & 0b01000000) > 0;
            boolean hasMethodName = (state & 0b00100000) > 0;
            String fn = hasFileName ? in.readUTF() : null;
            String cn = hasClassName ? in.readUTF() : null;
            String mn = hasMethodName ? in.readUTF() : null;
            stes[i] = new StackTraceElement(cn, mn, fn, ln);
        }
        th.setStackTrace(stes);
        int suppressedCount = in.readInt();
        for (int i = 0; i < suppressedCount; i++) {
            th.addSuppressed(rt(in));
        }
        boolean hasCause = in.readBoolean();
        if (hasCause) {
            th.initCause(rt(in));
        }
        return th;
    }

    /**
     * Unknown exception.
     */
    public static final class UnknownException extends Exception {

        private final String exceptionClassName;

        /**
         * Default constructor.
         */
        public UnknownException() {
            this(null, null);
        }

        /**
         * Constructs an unknown exception.
         * @param msg Message.
         * @param cl Exception class.
         */
        public UnknownException(String msg, String cl) {
            super(msg, null, false, false);
            exceptionClassName = cl;
        }

        /**
         * Get the exception class name.
         * @return Exception class name.
         */
        public String getExceptionClassName() {
            return exceptionClassName;
        }
    }
}
