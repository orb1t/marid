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

package org.marid.swing.log;

import java.awt.*;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.*;

import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class TrayIconHandler extends Handler {

    private final TrayIcon trayIcon;

    private TrayIconHandler(TrayIcon trayIcon, Level level) {
        this.trayIcon = trayIcon;
        setLevel(level);
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        final String caption = new Timestamp(record.getMillis()).toString();
        final String message = m(String.valueOf(record.getMessage()));
        String text = message;
        if (record.getParameters() != null && record.getParameters().length > 0) {
            try {
                text = MessageFormat.format(message, record.getParameters());
            } catch (Exception x) {
                text = message + Arrays.deepToString(record.getParameters());
            }
        }
        final TrayIcon.MessageType messageType;
        if (Level.INFO.equals(record.getLevel())) {
            messageType = TrayIcon.MessageType.INFO;
        } else if (Level.WARNING.equals(record.getLevel())) {
            messageType = TrayIcon.MessageType.WARNING;
        } else if (Level.SEVERE.equals(record.getLevel())) {
            messageType = TrayIcon.MessageType.ERROR;
        } else {
            messageType = TrayIcon.MessageType.NONE;
        }
        if (messageType != TrayIcon.MessageType.NONE) {
            try {
                trayIcon.displayMessage(caption, text, messageType);
            } catch (Exception x) {
                reportError("Display message error", x, ErrorManager.WRITE_FAILURE);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        final Logger logger = Logger.getLogger("");
        if (logger != null) {
            logger.removeHandler(this);
        }
    }

    public static void setHandlerLevel(TrayIcon trayIcon, Level level) {
        final Logger logger = Logger.getLogger("");
        if (logger != null) {
            for (final Handler handler : logger.getHandlers()) {
                if (handler instanceof TrayIconHandler && ((TrayIconHandler) handler).trayIcon == trayIcon) {
                    handler.setLevel(level);
                    break;
                }
            }
        }
    }

    public static void addSystemHandler(TrayIcon trayIcon, Level level) {
        final Logger logger = Logger.getLogger("");
        if (logger != null) {
            for (final Handler h : logger.getHandlers()) {
                if (h instanceof TrayIconHandler && ((TrayIconHandler) h).trayIcon == trayIcon) {
                    return;
                }
            }
            logger.addHandler(new TrayIconHandler(trayIcon, level));
        }
    }
}
