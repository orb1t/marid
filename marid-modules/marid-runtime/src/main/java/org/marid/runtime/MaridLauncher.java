/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime;


import org.marid.beans.RuntimeBean;
import org.marid.runtime.context.BeanConfiguration;
import org.marid.runtime.context.BeanContext;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;
import static org.marid.io.Xmls.read;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

  public static void main(String... args) throws Exception {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final URL beansXmlUrl = classLoader.getResource("META-INF/marid/beans.xml");
    if (beansXmlUrl == null) {
      throw new IllegalStateException("No beans.xml file found");
    }

    final AtomicReference<BeanContext> contextRef = new AtomicReference<>();
    daemonThread(contextRef).start();

    try (final Reader reader = new InputStreamReader(beansXmlUrl.openStream(), UTF_8)) {
      final BeanConfiguration configuration = new BeanConfiguration(classLoader, System.getProperties());
      final RuntimeBean bean = read(reader, e -> new RuntimeBean(null, e));
      final BeanContext context = new BeanContext(configuration, bean);
      contextRef.set(context);
    } catch (Throwable x) {
      x.printStackTrace();
    }
  }

  private static Thread daemonThread(AtomicReference<? extends AutoCloseable> contextRef) {
    final Thread daemon = new Thread(null, () -> {
      final Scanner scanner = new Scanner(System.in);
      try {
        while (scanner.hasNextLine()) {
          final String line = scanner.nextLine().trim();
          if (line.isEmpty()) {
            continue;
          }
          System.err.println(line);
          switch (line) {
            case "close":
              try {
                final AutoCloseable context = contextRef.get();
                if (context != null) {
                  context.close();
                  contextRef.set(null);
                }
              } catch (Exception x) {
                x.printStackTrace();
              }
              break;
            case "exit":
              System.exit(1);
              break;
          }
        }
      } catch (Exception x) {
        log(WARNING, "Command processing error", x);
      }
    }, "repl", 96L * 1024L);
    daemon.setDaemon(true);
    return daemon;
  }
}
