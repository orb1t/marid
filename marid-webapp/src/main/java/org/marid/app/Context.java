/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

package org.marid.app;

import org.marid.app.ui.UIExcludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.context.annotation.FilterType.CUSTOM;

@EnableScheduling
@SpringBootApplication
@ComponentScan(
    excludeFilters = {
        @ComponentScan.Filter(type = CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = CUSTOM, classes = AutoConfigurationExcludeFilter.class),
        @ComponentScan.Filter(type = CUSTOM, classes = UIExcludeFilter.class)
    }
)
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
  @Scope(SCOPE_PROTOTYPE)
  public static Logger logger(InjectionPoint point) {
    final Class<?> type = point.getMember().getDeclaringClass();
    return LoggerFactory.getLogger(type);
  }

  public static void main(String... args) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      e.printStackTrace();
    });
    new SpringApplicationBuilder(Context.class)
        .run(args);
  }
}
