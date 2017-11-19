/*-
 * #%L
 * marid-ide
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

package org.marid.ide;

import org.marid.ide.common.IdeValues;
import org.marid.ide.common.MaridDirectories;
import org.marid.ide.logging.IdeLogConsoleHandler;
import org.marid.ide.logging.IdeLogHandler;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Dmitry Ovchinnikov
 */
@Import({IdeDependants.class})
@ComponentScan
@EnableScheduling
@PropertySource({"meta.properties", "ide.properties"})
@Configuration
public class IdeContext {

  @Bean(destroyMethod = "shutdown")
  public static ScheduledThreadPoolExecutor scheduledExecutorService() {
    return new ScheduledThreadPoolExecutor(1);
  }

  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public Preferences preferences(InjectionPoint injectionPoint, IdeValues ideValues) {
    final Class<?> type = injectionPoint.getMember().getDeclaringClass();
    return Preferences.userNodeForPackage(type).node(type.getName()).node(ideValues.implementationVersion);
  }

  @Bean
  public MaridDirectories directories() {
    return new MaridDirectories();
  }

  @Bean
  public IdeLogHandler ideLogHandler() {
    return Stream.of(Logger.getLogger("").getHandlers())
        .filter(IdeLogHandler.class::isInstance)
        .map(IdeLogHandler.class::cast)
        .findAny()
        .orElseThrow(IllegalStateException::new);
  }

  @Bean
  public IdeLogConsoleHandler ideLogConsoleHandler() {
    return Stream.of(Logger.getLogger("").getHandlers())
        .filter(IdeLogConsoleHandler.class::isInstance)
        .map(IdeLogConsoleHandler.class::cast)
        .findAny()
        .orElseThrow(IllegalStateException::new);
  }

  @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
  public static ApplicationEventMulticaster multicaster(GenericApplicationContext context) {
    return new SimpleApplicationEventMulticaster(context.getBeanFactory());
  }
}
