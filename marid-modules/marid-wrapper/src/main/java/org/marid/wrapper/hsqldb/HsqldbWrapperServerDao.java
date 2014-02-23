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

package org.marid.wrapper.hsqldb;

import org.hsqldb.Session;
import org.hsqldb.Statement;
import org.hsqldb.result.Result;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class HsqldbWrapperServerDao {

    private final Session session;
    private final ConcurrentMap<String, Statement> statementMap = new ConcurrentHashMap<>();

    public HsqldbWrapperServerDao(Session session) {
        this.session = session;
        this.session.setAutoCommit(true);
    }

    public void initDefaultSchema() {
        update("ALTER SCHEMA PUBLIC RENAME TO wrapperSchema");
        update("SET INITIAL SCHEMA wrapperSchema");
        update("SET SCHEMA wrapperSchema");
    }

    private Statement compile(String sql) {
        return statementMap.computeIfAbsent(sql, session::compileStatement);
    }

    public int update(String sql) {
        final Result result = session.executeDirectStatement(sql);
        if (result.isUpdateCount()) {
            return result.getUpdateCount();
        } else if (result.isError() || result.isWarning()) {
            throw result.getException();
        } else if (result.isData() || result.isSimpleValue()) {
            throw new IllegalStateException();
        } else {
            throw new IllegalStateException();
        }
    }

    private Object[] transformParameters(Object[] parameters) {
        if (parameters == null) {
            return null;
        } else {
            final Object[] result = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                final Object p = parameters[i];
                if (p == null) {
                    result[i] = null;
                } else {
                    final Class<?> c = p.getClass().isArray() ? p.getClass().getComponentType() : p.getClass();
                    if (Number.class.isAssignableFrom(c)
                            || String.class == c
                            || Boolean.class == c
                            || Character.class == c
                            || Locale.class == c
                            || Date.class.isAssignableFrom(c)
                            || TimeZone.class.isAssignableFrom(c)
                            || Calendar.class.isAssignableFrom(c)
                            || Currency.class == c
                            || c.isPrimitive())  {
                        result[i] = p;
                    } else {
                        result[i] = p.toString();
                    }
                }
            }
            return result;
        }
    }

    public void writeLogRecord(LogRecord logRecord) {
        final String logger = logRecord.getLoggerName() == null ? "" : logRecord.getLoggerName();
        final int level = logRecord.getLevel() == null ? Level.OFF.intValue() : logRecord.getLevel().intValue();
        final String message = logRecord.getMessage() == null ? "" : logRecord.getMessage();
        final Long thrown = logRecord.getThrown() == null ? null : writeThrown(logRecord.getThrown());
        final Object[] parameters = transformParameters(logRecord.getParameters());
        final Timestamp timestamp = new Timestamp(logRecord.getMillis());
        final Statement statement = compile("INSERT INTO logRecords VALUES (?, ?, ?, ?, ?, ?)");
        final Result result;
        synchronized (statement) {
            result = session.executeCompiledStatement(statement, new Object[] {
                    logger, level, message, timestamp, parameters, thrown
            }, 1_000);
        }
        if (result.isError()) {
            throw result.getException();
        } else if (result.isUpdateCount()) {
            if (result.getUpdateCount() != 1) {
                throw new IllegalStateException(Integer.toString(result.getUpdateCount()));
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private long writeThrown(Throwable throwable) {
        final String type = throwable.getClass().getName();
        final Long cause = throwable.getCause() != null ? writeThrown(throwable.getCause()) : null;
        final StackTraceElement[] els = throwable.getStackTrace();
        final String[] fileNames = new String[els.length];
        final String[] classNames = new String[els.length];
        final String[] methodNames = new String[els.length];
        final int[] lineNumbers = new int[els.length];
        for (int i = 0; i < els.length; i++) {
            final StackTraceElement el = els[i];
            fileNames[i] = el.getFileName();
            classNames[i] = el.getClassName();
            methodNames[i] = el.getMethodName();
            lineNumbers[i] = el.getLineNumber();
        }
        final Throwable[] supressedThrowables = throwable.getSuppressed();
        final long[] supressed = new long[supressedThrowables.length];
        for (int i = 0; i < supressedThrowables.length; i++) {
            supressed[i] = writeThrown(supressedThrowables[i]);
        }
        final Statement statement = compile("INSERT INTO throwns VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        final Result result;
        synchronized (statement) {
            result = session.executeCompiledStatement(statement, new Object[]{
                    type, throwable.getMessage(), cause,
                    fileNames.length == 0 ? null : fileNames,
                    classNames.length == 0 ? null : classNames,
                    methodNames.length == 0 ? null : methodNames,
                    lineNumbers.length == 0 ? null : lineNumbers,
                    supressed.length == 0 ? null : supressed
            }, 1_000);
        }
        if (result.isError()) {
            throw result.getException();
        } else if (result.isUpdateCount()) {
            return session.getLastIdentity().longValue();
        } else {
            throw new IllegalStateException();
        }
    }
}
