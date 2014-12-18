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

package org.marid.test.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.Thread.currentThread;

/**
 * @author Dmitry Ovchinnikov.
 */
public class TestUtils {

    public static void changeWorkingDirectory() throws Exception {
        try (final InputStream is = currentThread().getContextClassLoader().getResourceAsStream("module.properties")) {
            if (is != null) {
                final Properties properties = new Properties();
                properties.load(is);
                if (properties.containsKey("module.build.directory")) {
                    final File moduleDirectory = new File(properties.getProperty("module.build.directory"));
                    System.setProperty("user.dir", moduleDirectory.getAbsolutePath());
                }
            }
        }
    }
}
