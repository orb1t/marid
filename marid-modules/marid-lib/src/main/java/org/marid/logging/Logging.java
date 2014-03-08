/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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

import org.marid.logging.monitoring.LogMXBean;
import org.marid.management.JmxUtils;
import org.marid.util.Utils;

import javax.management.MBeanServer;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Logging {

    public static final Level[] LEVELS = {
            Level.SEVERE,
            Level.WARNING,
            Level.CONFIG,
            Level.INFO,
            Level.FINE,
            Level.FINER,
            Level.FINEST
    };

    public static void init(Class<?> c, String res) {
        final ClassLoader cl = Utils.getClassLoader(c);
        try (final InputStream is = cl.getResourceAsStream(res)) {
            final LogManager lm = LogManager.getLogManager();
            if (is != null) {
                lm.readConfiguration(is);
            } else {
                try (final InputStream dis = cl.getResourceAsStream("marid-logging-default.properties")) {
                    if (dis != null) {
                        lm.readConfiguration(dis);
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
        final Logger root = Logger.getLogger("");
        if (root != null) {
            final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            for (final Handler handler : root.getHandlers()) {
                if (handler instanceof LogMXBean) {
                    try {
                        mBeanServer.registerMBean(handler, JmxUtils.getObjectName(handler.getClass()));
                    } catch (Exception x) {
                        x.printStackTrace(System.err);
                    }
                }
            }
        }
    }
}
