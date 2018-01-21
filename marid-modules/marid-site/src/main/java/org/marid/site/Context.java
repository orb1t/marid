/*-
 * #%L
 * marid-site
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

package org.marid.site;

import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.EnableVaadinNavigation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Scanner;

@EnableScheduling
@EnableWebMvc
@EnableVaadinNavigation
@EnableVaadin
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
            System.exit(0);
            break;
        }
      }
    }, "shutdownThread", 96L * 1024L, false);
    thread.setDaemon(true);
    return thread;
  }

  public static void main(String... args) {
    SpringApplication.run(Context.class, args);
  }
}
