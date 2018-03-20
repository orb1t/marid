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

import org.marid.app.annotation.PrototypeScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@EnableScheduling
@ComponentScan
@PropertySource(value = {"classpath:application.properties"})
@PropertySource(value = {"classpath:additional.properties"}, ignoreResourceNotFound = true)
public class Context {

  @Bean(initMethod = "start")
  public Thread shutdownThread(ConfigurableApplicationContext context) {
    final Thread thread = new Thread(null, () -> {
      final Scanner scanner = new Scanner(System.in);
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().trim();
        switch (line) {
          case "exit":
            try {
              context.close();
            } catch (Exception x) {
              x.printStackTrace();
            }
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

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setFileEncoding("UTF-8");
    configurer.setNullValue("@null");
    configurer.setIgnoreResourceNotFound(true);
    configurer.setIgnoreUnresolvablePlaceholders(false);
    return configurer;
  }

  public static void main(String... args) {
    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.setId("marid");
    context.setDisplayName("Marid Web Application");
    context.setAllowCircularReferences(false);
    context.setAllowBeanDefinitionOverriding(false);
    context.register(Context.class);
    context.refresh();
    context.start();
  }
}
