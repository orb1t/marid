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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.marid.util.UnknownException;

/**
 * Log recordset.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class LogRecordSet implements Externalizable {

    private final ArrayList<LogRecord> logRecordList;

    /**
     * Default constructor.
     */
    public LogRecordSet() {
        this(Collections.<LogRecord>emptyList());
    }

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
        for (LogRecord r : logRecordList) {
            out.writeLong(r.getMillis());
            out.writeLong(r.getSequenceNumber());
            out.writeInt(r.getThreadID());
            BitSet bs = new BitSet(8);
            bs.set(0, r.getLevel() != null);
            bs.set(1, r.getMessage() != null);
            bs.set(2, r.getSourceClassName() != null);
            bs.set(3, r.getSourceMethodName() != null);
            bs.set(4, r.getLoggerName() != null);
            bs.set(5, r.getResourceBundleName() != null);
            bs.set(6, r.getParameters() != null);
            bs.set(7, r.getThrown() != null);
            byte[] buf = bs.toByteArray();
            if (buf.length == 0) {
                out.write(0);
            } else {
                out.write(buf[0]);
            }
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
                writeThrown(out, r.getThrown());
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
            BitSet bs = BitSet.valueOf(new byte[] {in.readByte()});
            Level level = bs.get(0) ? Level.parse(in.readUTF()) : Level.OFF;
            String message = bs.get(1) ? in.readUTF() : null;
            String sourceClassName = bs.get(2) ? in.readUTF() : null;
            String sourceMethodName = bs.get(3) ? in.readUTF() : null;
            String loggerName = bs.get(4) ? in.readUTF() : null;
            String rbName = bs.get(5) ? in.readUTF() : null;
            Object[] params = null;
            LogRecord r = new LogRecord(level, message);
            r.setLoggerName(loggerName);
            r.setResourceBundleName(rbName);
            r.setSourceClassName(sourceClassName);
            r.setSourceMethodName(sourceMethodName);
            r.setThreadID(threadId);
            r.setSequenceNumber(sequenceNumber);
            r.setMillis(millis);
            if (bs.get(6)) {
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
            if (bs.get(7)) {
                r.setThrown(readThrown(in));
            }
            logRecordList.add(r);
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (LogRecord r : logRecordList) {
            hash = 31 * hash + hashCode(r);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayList<LogRecord> l1 = logRecordList;
        ArrayList<LogRecord> l2 = ((LogRecordSet)obj).logRecordList;
        if (l1.size() != l2.size()) {
            return false;
        }
        for (int i = 0; i < l1.size(); i++) {
            if (!equals(l1.get(i), l2.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('(');
        sb.append(logRecordList.size());
        sb.append(')');
        return sb.toString();
    }

    void writeThrown(ObjectOutput out, Throwable th) throws IOException {
        boolean hasCause = th.getCause() != null;
        out.writeBoolean(hasCause);
        if (hasCause) {
            writeThrown(out, th.getCause());
        }
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
            BitSet bs = new BitSet(3);
            bs.set(0, ste.getFileName() != null);
            bs.set(1, ste.getClassName() != null);
            bs.set(2, ste.getMethodName() != null);
            byte[] buf = bs.toByteArray();
            if (buf.length == 0) {
                out.write(0);
            } else {
                out.write(buf[0]);
            }
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
            writeThrown(out, suppressed);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    Throwable readThrown(ObjectInput in) throws IOException {
        boolean hasCause = in.readBoolean();
        Throwable cause = null;
        if (hasCause) {
            cause = readThrown(in);
        }
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
            if (hasMessage) {
                if (hasCause) {
                    try {
                        th = (Throwable)c.getDeclaredConstructor(String.class,
                                Throwable.class).newInstance(msg, cause);
                    } catch (Exception x) {
                        th = (Throwable)c.getDeclaredConstructor(
                                String.class).newInstance(msg);
                        th.initCause(cause);
                    }
                } else {
                    try {
                        th = (Throwable)c.getDeclaredConstructor(
                                String.class).newInstance(msg);
                    } catch (Exception x) {
                        th = (Throwable)c.getDeclaredConstructor(String.class,
                                Throwable.class).newInstance(msg, cause);
                    }
                }
            } else {
                if (hasCause) {
                    try {
                        th = (Throwable)c.getDeclaredConstructor(
                                Throwable.class).newInstance(cause);
                    } catch (Exception x) {
                        th = (Throwable)c.newInstance();
                        th.initCause(cause);
                    }
                } else {
                    th = (Throwable)c.newInstance();
                }
            }
        } catch (Exception x) {
            th = new UnknownException(msg, cause, thc);
        }
        StackTraceElement[] stes = new StackTraceElement[in.readInt()];
        for (int i = 0; i < stes.length; i++) {
            int ln = in.readInt();
            BitSet bs = BitSet.valueOf(new byte[] {in.readByte()});
            String fn = bs.get(0) ? in.readUTF() : null;
            String cn = bs.get(1) ? in.readUTF() : null;
            String mn = bs.get(2) ? in.readUTF() : null;
            stes[i] = new StackTraceElement(cn, mn, fn, ln);
        }
        th.setStackTrace(stes);
        int suppressedCount = in.readInt();
        for (int i = 0; i < suppressedCount; i++) {
            th.addSuppressed(readThrown(in));
        }
        return th;
    }

    boolean equals(LogRecord r1, LogRecord r2) {
        return
                Objects.equals(r1.getLevel(), r2.getLevel()) &&
                Objects.equals(r1.getLoggerName(), r2.getLoggerName()) &&
                Objects.equals(r1.getMessage(), r2.getMessage()) &&
                Objects.equals(r1.getMillis(), r2.getMillis()) &&
                Objects.deepEquals(r1.getParameters(), r2.getParameters()) &&
                Objects.equals(r1.getResourceBundleName(),
                    r2.getResourceBundleName()) &&
                Objects.equals(r1.getSequenceNumber(),
                    r2.getSequenceNumber()) &&
                Objects.equals(r1.getSourceClassName(),
                    r2.getSourceClassName()) &&
                Objects.equals(r1.getSourceMethodName(),
                    r2.getSourceMethodName()) &&
                Objects.equals(r1.getThreadID(), r2.getThreadID()) &&
                equals(r1.getThrown(), r2.getThrown());
    }

    boolean equals(Throwable t1, Throwable t2) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        if (!Objects.equals(t1.getMessage(), t2.getMessage())) {
            return false;
        }
        if (!Objects.deepEquals(t1.getStackTrace(), t2.getStackTrace())) {
            return false;
        }
        Throwable[] sps1 = t1.getSuppressed();
        Throwable[] sps2 = t2.getSuppressed();
        if (sps1.length != sps2.length) {
            return false;
        }
        for (int i = 0; i < sps1.length; i++) {
            if (!equals(sps1[i], sps2[i])) {
                return false;
            }
        }
        return equals(t1.getCause(), t2.getCause());
    }

    int hashCode(LogRecord r) {
        int hash = 1;
        hash = 31 * hash + Objects.hash(
                r.getLevel(),
                r.getLoggerName(),
                r.getMessage(),
                r.getMillis(),
                r.getResourceBundleName(),
                r.getSequenceNumber(),
                r.getSourceClassName(),
                r.getSourceMethodName(),
                r.getThreadID());
        hash = 31 * hash + Objects.hash(r.getParameters());
        hash = 31 * hash + hashCode(r.getThrown());
        return hash;
    }

    int hashCode(Throwable t) {
        if (t == null) {
            return 0;
        }
        int hash = 1;
        hash = 31 * hash + Objects.hashCode(t.getMessage());
        hash = 31 * hash + (t.getCause() != null ? hashCode(t.getCause()) : 0);
        for (Throwable th : t.getSuppressed()) {
            hash = 31 * hash + hashCode(th);
        }
        hash = 31 * hash + Objects.hash((Object[])t.getStackTrace());
        return hash;
    }
}
