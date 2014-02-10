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

package org.marid.swing.log;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.lang.Thread.currentThread;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingLogPanel extends JPanel implements AncestorListener {

    private static final Logger LOG = Logger.getLogger(SwingLogPanel.class.getName());
    private final Logger[] loggers;
    private final LogHandler logHandler;
    private final Preferences prefs;

    public SwingLogPanel(Preferences prefs, Logger... loggers) {
        this.prefs = prefs;
        this.loggers = loggers;
        this.logHandler = new LogHandler();
    }

    public SwingLogPanel(Preferences prefs) {
        this(prefs, Logger.getLogger(""));
    }

    public Handler getLogHandler() {
        return logHandler;
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        logHandler.register();
    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {
        logHandler.close();
    }

    @Override
    public void ancestorMoved(AncestorEvent event) {
    }

    private class LogHandler extends Handler {

        public LogHandler() {
            String formatter = prefs.get("formatter", null);
            if (formatter != null) {
                try {
                    Class<?> c = currentThread().getContextClassLoader().loadClass(formatter);
                    setFormatter(Formatter.class.cast(c.newInstance()));
                } catch (Exception x) {
                    warning(LOG, "Unable to create a formatter", x);
                }
            }
        }

        public void register() {
            for (Logger logger : loggers) {
                logger.addHandler(logHandler);
            }
        }

        @Override
        public void publish(LogRecord record) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            for (Logger logger : loggers) {
                logger.removeHandler(this);
            }
        }
    }
}
