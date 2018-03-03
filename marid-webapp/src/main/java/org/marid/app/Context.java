/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.app;

import j2html.Config;
import org.marid.app.annotation.PrototypeScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@EnableScheduling
@SpringBootApplication
public class Context {

  @Bean(initMethod = "start")
  public Thread shutdownThread(ConfigurableApplicationContext context) {
    final Thread thread = new Thread(null, () -> {
      final Scanner scanner = new Scanner(System.in);
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().trim();
        switch (line) {
          case "exit":
            SpringApplication.exit(context);
            break;
          case "quit":
          case "q":
            System.exit(0);
            break;
        }
      }
    }, "shutdownThread", 96L * 1024L, false);
    thread.setDaemon(true);
    return thread;
  }

  @Bean
  @PrototypeScoped
  public static Logger logger(InjectionPoint point) {
    final Class<?> type = point.getMember().getDeclaringClass();
    return LoggerFactory.getLogger(type);
  }

  public static void main(String... args) {
    Config.closeEmptyTags = true;
    SpringApplication.run(Context.class, args);
  }
}
