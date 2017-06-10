/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.runtime;

import org.jboss.weld.environment.se.Weld;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author Dmitry Ovchinnikov
 */
public class StandardMaridWeldInitializer implements WeldInitializer {

    @Override
    public void initialize(Weld weld) throws Exception {
        weld.addBeanClass(MaridRuntime.class);

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = classLoader.getResourceAsStream("bean-classes.lst")) {
            if (stream != null) {
                try (final Scanner scanner = new Scanner(stream, "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        weld.addBeanClass(Class.forName(line, true, classLoader));
                    }
                }
            }
        }

        try (final InputStream stream = classLoader.getResourceAsStream("bean-packages.lst")) {
            if (stream != null) {
                try (final Scanner scanner = new Scanner(stream, "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        if (line.endsWith("*")) {
                            weld.addPackages(true, Package.getPackage(line.substring(0, line.length() - 1)));
                        } else {
                            weld.addPackages(false, Package.getPackage(line));
                        }
                    }
                }
            }
        }
    }
}
