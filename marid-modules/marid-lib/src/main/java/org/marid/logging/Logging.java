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

import javax.management.MBeanServer;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.marid.util.Utils.currentClassLoader;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Logging {

    static final boolean LOGGING_DOMAIN_ENABLED;
    static final Properties LOGGING_DOMAIN_PROPERTIES = new Properties();
    static final InheritableThreadLocal<String> PREFIX_ITL = new InheritableThreadLocal<>();

    static {
        try (final InputStream is = currentClassLoader().getResourceAsStream("META-INF/logging-domain.properties")) {
            if (is != null) {
                LOGGING_DOMAIN_PROPERTIES.load(is);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        LOGGING_DOMAIN_ENABLED = "true".equals(LOGGING_DOMAIN_PROPERTIES.getProperty("enabled"));
    }

    public static final Level[] LEVELS = {
            Level.SEVERE,
            Level.WARNING,
            Level.CONFIG,
            Level.INFO,
            Level.FINE,
            Level.FINER,
            Level.FINEST
    };

    public static void init(String res) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
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

    static class LoggingClassValue extends ClassValue<Logger> {
        @Override
        protected Logger computeValue(Class<?> type) {
            return Logger.getLogger(type.getName());
        }
    }

    static class LoggingDomainClassValue extends ClassValue<Logger> {
        @Override
        public Logger get(Class<?> type) {
            final String prefix = PREFIX_ITL.get();
            if (prefix == null) {
                return super.get(type);
            } else {
                final Logger logger = super.get(type);
                if (logger.getName().startsWith(prefix)) {
                    return logger;
                } else {
                    remove(type);
                    return get(type);
                }
            }
        }

        @Override
        protected Logger computeValue(Class<?> type) {
            final String prefix = PREFIX_ITL.get();
            return Logger.getLogger(prefix == null ? type.getName() : prefix + type.getName());
        }
    }
}
