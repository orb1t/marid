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
package org.marid;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Java abstract service.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class JavaAbstractService extends AbstractService {

    @SuppressWarnings("NonConstantLogger")
    protected final Logger log;

    /**
     * Constructs an abstract service.
     */
    public JavaAbstractService() {
        log = Logger.getLogger(getClass().getName(), "res.messages");
    }

    /**
     * Get the filled log record.
     * @param l Log level.
     * @param m Log message.
     * @param x Record error field.
     * @param p Record parameter.
     * @return Filled log record.
     */
    protected final LogRecord l(Level l, String m, Throwable x, Object... p) {
        LogRecord lr = new LogRecord(l, m);
        lr.setThrown(x);
        lr.setParameters(p);
        return lr;
    }

    /**
     * Get the filled log record.
     * @param l Log level.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled log record.
     */
    protected final LogRecord l(Level l, String m, Object... p) {
        LogRecord lr = new LogRecord(l, m);
        lr.setParameters(p);
        return lr;
    }

    /**
     * Get the information log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled information log record.
     */
    protected final LogRecord i(String m, Object... p) {
        return l(Level.INFO, m, p);
    }

    /**
     * Get the warning log record.
     * @param m Log message.
     * @param x Record error field.
     * @param p Parameters.
     * @return Filled warning log record.
     */
    protected final LogRecord w(String m, Throwable x, Object... p) {
        return l(Level.WARNING, m, x, p);
    }

    /**
     * Get the warning log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled warning log record.
     */
    protected final LogRecord w(String m, Object... p) {
        return l(Level.WARNING, m, p);
    }

    /**
     * Get the severe log record.
     * @param m Log message.
     * @param x Record error field.
     * @param p Parameters.
     * @return Filled severe log record.
     */
    protected final LogRecord s(String m, Throwable x, Object... p) {
        return l(Level.SEVERE, m, x, p);
    }

    /**
     * Get the severe log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled severe log record.
     */
    protected final LogRecord s(String m, Object... p) {
        return l(Level.SEVERE, m, p);
    }

    /**
     * Get the config log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled config log record.
     */
    protected final LogRecord c(String m, Object... p) {
        return l(Level.CONFIG, m, p);
    }

    /**
     * Get the fine log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled fine log record.
     */
    protected final LogRecord f(String m, Object... p) {
        return l(Level.FINE, m, p);
    }

    /**
     * Get the finer (good) log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled finer log record.
     */
    protected final LogRecord g(String m, Object... p) {
        return l(Level.FINER, m, p);
    }

    /**
     * Get the finest (best) log record.
     * @param m Log message.
     * @param p Parameters.
     * @return Filled finest log record.
     */
    protected final LogRecord b(String m, Object... p) {
        return l(Level.FINEST, m, p);
    }
}
