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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class Versioning {

    private static final Logger LOG = Logger.getLogger(Versioning.class.getName());
    private static final Properties GLOBALS = new Properties();

    static {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("meta.properties");
        if (is != null) {
            try (final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                GLOBALS.load(reader);
            } catch (Exception x) {
                log(LOG, WARNING, "Unable to read global meta.properties", x);
            }
        }
    }

    private static final ClassValue<Properties> LOCALS = new ClassValue<Properties>() {
        @Override
        protected Properties computeValue(Class<?> type) {
            final ProtectionDomain protectionDomain = type.getProtectionDomain();
            if (protectionDomain == null) {
                return GLOBALS;
            } else {
                final CodeSource codeSource = protectionDomain.getCodeSource();
                if (codeSource == null) {
                    return GLOBALS;
                } else {
                    try (final URLClassLoader cl = new URLClassLoader(new URL[] {codeSource.getLocation()})) {
                        final InputStream is = cl.getResourceAsStream("meta.properties");
                        if (is != null) {
                            final Properties properties = new Properties();
                            try (final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                                properties.load(reader);
                            }
                            return properties;
                        } else {
                            return GLOBALS;
                        }
                    } catch (Exception x) {
                        log(LOG, WARNING, "Unable to read meta.properties", x);
                        return GLOBALS;
                    }
                }
            }
        }
    };

    public static String getImplementationVersion(Class<?> type) {
        final String version = type.getPackage().getImplementationVersion();
        return version != null ? version : extractField(type, "implementation.version");
    }

    public static String getImplementationTitle(Class<?> type) {
        final String title = type.getPackage().getImplementationTitle();
        return title != null ? title : extractField(type, "implementation.title");
    }

    public static String getImplementationVendor(Class<?> type) {
        final String vendor = type.getPackage().getImplementationVendor();
        return vendor != null ? vendor : extractField(type, "implementation.vendor");
    }

    public static String getSpecificationVersion(Class<?> type) {
        final String version = type.getPackage().getSpecificationVersion();
        return version != null ? version : extractField(type, "specification.version");
    }

    public static String getSpecificationTitle(Class<?> type) {
        final String title = type.getPackage().getSpecificationTitle();
        return title != null ? title : extractField(type, "specification.title");
    }

    public static String getSpecificationVendor(Class<?> type) {
        final String vendor = type.getPackage().getSpecificationVendor();
        return vendor != null ? vendor : extractField(type, "specification.vendor");
    }

    public static String extractField(Class<?> type, String field) {
        return LOCALS.get(type).getProperty(field, "-");
    }
}
