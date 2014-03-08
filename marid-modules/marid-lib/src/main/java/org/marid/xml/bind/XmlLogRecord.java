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

package org.marid.xml.bind;

import org.marid.util.ReflectionUtils;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.time.Instant.ofEpochMilli;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "record")
@XmlSeeAlso({XmlThrowable.class})
public class XmlLogRecord extends ReflectionUtils.HET {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @XmlAttribute(name = "ln")
    private final String loggerName;

    @XmlAttribute(name = "l")
    private final String level;

    @XmlAttribute
    private final String timestamp;

    @XmlAttribute(name = "th")
    private final int threadId;

    @XmlAttribute(name = "msg")
    private final String message;

    @XmlAttribute(name = "c")
    private final String className;

    @XmlAttribute(name = "m")
    private final String methodName;

    @XmlElement(name = "thrown")
    private final XmlThrowable thrown;

    @XmlElementWrapper(name = "ps")
    @XmlElements({
            @XmlElement(type = String.class, name = "string"),
            @XmlElement(type = Integer.class, name = "int"),
            @XmlElement(type = Long.class, name = "long"),
            @XmlElement(type = Double.class, name = "double"),
            @XmlElement(type = Float.class, name = "float"),
            @XmlElement(type = Boolean.class, name = "boolean"),
            @XmlElement(type = NullValue.class, name = "null"),
            @XmlElement(type = byte[].class, name = "binary"),
            @XmlElement(type = Short.class, name = "short"),
            @XmlElement(type = Byte.class, name = "byte"),
            @XmlElement(type = Character.class, name = "char"),
            @XmlElement(type = int[].class, name = "ints"),
            @XmlElement(type = long[].class, name = "longs"),
            @XmlElement(type = char[].class, name = "chars"),
            @XmlElement(type = double[].class, name = "doubles"),
            @XmlElement(type = float[].class, name = "floats"),
            @XmlElement(type = boolean[].class, name = "bools"),
            @XmlElement(type = BigInteger.class, name = "bigint"),
            @XmlElement(type = BigDecimal.class, name = "number"),
            @XmlElement(type = short[].class, name = "shorts")
    })
    private final Object[] parameters;

    public XmlLogRecord() {
        loggerName = null;
        level = null;
        timestamp = null;
        threadId = 0;
        message = null;
        className = null;
        methodName = null;
        parameters = null;
        thrown = null;
    }

    public XmlLogRecord(LogRecord logRecord) {
        loggerName = logRecord.getLoggerName();
        level = logRecord.getLevel().getName();
        timestamp = Instant.ofEpochMilli(logRecord.getMillis()).toString();
        threadId = logRecord.getThreadID();
        message = logRecord.getMessage();
        className = logRecord.getSourceClassName();
        methodName = logRecord.getSourceMethodName();
        if (logRecord.getParameters() == null) {
            parameters = null;
        } else {
            parameters = new Object[logRecord.getParameters().length];
            for (int i = 0; i < parameters.length; i++) {
                final Object v = logRecord.getParameters()[i];
                if (v == null) {
                    parameters[i] = new NullValue();
                } else if (v instanceof String
                        || v instanceof Integer
                        || v instanceof Long
                        || v instanceof Double
                        || v instanceof Float
                        || v instanceof Boolean
                        || v instanceof Void
                        || v instanceof NullValue
                        || v.getClass().isArray() && v.getClass().getComponentType().isPrimitive()
                        || v instanceof Short
                        || v instanceof Byte
                        || v instanceof Character
                        || v instanceof char[]
                        || v instanceof Calendar
                        || v instanceof BigInteger
                        || v instanceof BigDecimal) {
                    parameters[i] = v;
                } else if (v instanceof Date) {
                    parameters[i] = OffsetDateTime.ofInstant(ofEpochMilli(((Date) v).getTime()), ZONE_ID).toString();
                } else {
                    parameters[i] = v.toString();
                }
            }
        }
        thrown = logRecord.getThrown() == null ? null : new XmlThrowable(logRecord.getThrown());
    }

    public LogRecord toLogRecord() {
        final LogRecord logRecord = new LogRecord(Level.parse(level), message);
        logRecord.setLoggerName(loggerName);
        if (thrown != null) {
            logRecord.setThrown(thrown.toThrowable());
        }
        logRecord.setThreadID(threadId);
        logRecord.setMillis(Instant.parse(timestamp).toEpochMilli());
        logRecord.setSourceClassName(className);
        logRecord.setSourceMethodName(methodName);
        logRecord.setParameters(parameters);
        return logRecord;
    }

    public String getMessage() {
        return message;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getMillis() {
        return Instant.parse(timestamp).toEpochMilli();
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public XmlThrowable getThrown() {
        return thrown;
    }

    @XmlType
    private static class NullValue {
    }
}
