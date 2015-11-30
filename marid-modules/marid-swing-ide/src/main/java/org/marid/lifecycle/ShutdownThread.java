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

package org.marid.lifecycle;

import org.marid.logging.LogSupport;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ShutdownThread extends Thread implements LogSupport {

    private final ConfigurableApplicationContext context;
    private final String pidFileName;
    private final WatchService watchService;

    public ShutdownThread(ConfigurableApplicationContext context) {
        super(Thread.currentThread().getThreadGroup(), null, "shutdownThread", 64 * 1024);
        this.context = context;
        setDaemon(true);
        try {
            final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            final String name = runtimeMXBean.getName();
            final int index = name.indexOf('@');
            final int pid = Integer.parseInt(name.substring(0, index));
            final Path pidFile = Paths.get(System.getProperty("user.dir"), "pid.file");
            pidFileName = pidFile.getFileName().toString();
            pidFile.toFile().deleteOnExit();
            Files.write(pidFile, Integer.toString(pid).getBytes(StandardCharsets.UTF_8));
            watchService = pidFile.getFileSystem().newWatchService();
            pidFile.getParent().register(watchService, StandardWatchEventKinds.ENTRY_DELETE);
        } catch (Exception x) {
            throw new IllegalStateException("Unable to write PID file", x);
        }
    }

    @Override
    public void run() {
        try (final WatchService ws = watchService) {
            while (true) {
                final WatchKey watchKey = ws.take();
                try {
                    for (final WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                        final Path path = (Path) watchEvent.context();
                        if (path.getFileName().toString().equals(pidFileName)) {
                            context.close();
                            return;
                        }
                    }
                } catch (Exception x) {
                    log(WARNING, "Unable to process {0}", x, watchKey);
                } finally {
                    watchKey.reset();
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to watch events", x);
        }
    }
}
