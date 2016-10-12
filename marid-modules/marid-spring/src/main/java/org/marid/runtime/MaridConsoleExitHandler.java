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

import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Scanner;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridConsoleExitHandler implements LogSupport {

    private final MaridCloseAction closeAction;

    @Autowired
    public MaridConsoleExitHandler(MaridCloseAction closeAction) {
        this.closeAction = closeAction;
    }

    @PostConstruct
    private void init() {
        final Thread thread = new Thread(null, () -> {
            try (final Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine().trim();
                    try {
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
                            case "close":
                                closeAction.run();
                                break;
                            case "exit":
                            case "quit":
                                System.exit(0);
                                break;
                        }
                    } catch (Exception x) {
                        log(WARNING, "Unable to execute {0}", x, line);
                    }
                }
            }
        }, "console-daemon", 64L * 1024L);
        thread.setDaemon(true);
        thread.start();
    }
}
