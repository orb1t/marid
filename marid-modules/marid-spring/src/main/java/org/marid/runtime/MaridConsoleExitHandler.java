/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

import org.springframework.context.support.AbstractApplicationContext;

import java.util.Scanner;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridConsoleExitHandler {

    public static void handle(AbstractApplicationContext applicationContext) {
        try (final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();
                switch (line) {
                    case "dump":
                        Thread.getAllStackTraces().forEach((t, stes) -> {
                            System.err.println(t);
                            if (stes != null) {
                                for (final StackTraceElement e : stes) {
                                    System.err.format("%s %s.%s:%d%n",
                                            e.getFileName(),
                                            e.getClassName(),
                                            e.getMethodName(),
                                            e.getLineNumber());
                                }
                            }
                            System.err.println();
                        });
                        break;
                    case "exit":
                    case "quit":
                        applicationContext.close();
                        break;
                }
            }
        }
    }
}
