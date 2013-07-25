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

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Versioning {

    private static final Logger LOG = Logger.getLogger(Versioning.class.getName());

    public static String getImplementationVersion(Class<?> type) {
        String version = type.getPackage().getImplementationVersion();
        return version != null ? version : extractField(type, "implementation.version");
    }

    private static String extractField(Class<?> type, String field) {
        ProtectionDomain protectionDomain = type.getProtectionDomain();
        if (protectionDomain == null) {
            return extractGlobalField(field);
        } else {
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null) {
                return extractGlobalField(field);
            } else {
                try (URLClassLoader cl = new URLClassLoader(new URL[] {codeSource.getLocation()})) {
                    InputStream is = cl.getResourceAsStream("meta.properties");
                    if (is != null) {
                        try (Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                            Properties properties = new Properties();
                            properties.load(r);
                            if (properties.containsKey(field)) {
                                return properties.getProperty(field);
                            } else {
                                return extractGlobalField(field);
                            }
                        }
                    } else {
                        return extractGlobalField(field);
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to read meta.properties", x);
                    return extractGlobalField(field);
                }
            }
        }
    }

    private static String extractGlobalField(String field) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = Versioning.class.getClassLoader();
        }
        InputStream is = classLoader.getResourceAsStream("meta.properties");
        if (is != null) {
            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties.getProperty(field, "-");
            } catch (Exception x) {
                warning(LOG, "Unable to read any meta.properties", x);
                return "-";
            }
        } else {
            return "-";
        }
    }
}
