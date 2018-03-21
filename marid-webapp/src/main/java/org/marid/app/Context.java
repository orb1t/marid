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
import org.marid.app.logging.MaridLogging;
import org.marid.app.spring.LoggingPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@EnableScheduling
@ComponentScan(basePackageClasses = {Context.class})
@PropertySource(value = {"classpath:application.properties"})
@Configuration
public class Context {

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
    configurer.setIgnoreResourceNotFound(false);
    configurer.setIgnoreUnresolvablePlaceholders(false);
    return configurer;
  }

  public static void main(String... args) throws Exception {
    MaridLogging.initLogging();

    final File pidFile = new File("marid-webapp.pid");
    pidFile.deleteOnExit();

    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    context.setId("marid");
    context.setDisplayName("Marid Web Application");
    context.setAllowCircularReferences(false);
    context.setAllowBeanDefinitionOverriding(false);
    context.getBeanFactory().addBeanPostProcessor(new LoggingPostProcessor());
    context.registerShutdownHook();
    context.register(Context.class);
    context.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
    context.refresh();
    context.start();

    Files.write(pidFile.toPath(), List.of(Long.toString(ProcessHandle.current().pid())));
  }
}
