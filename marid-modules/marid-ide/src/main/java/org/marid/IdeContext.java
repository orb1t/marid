/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid;

import org.marid.ide.common.IdeValues;
import org.marid.ide.common.MaridDirectories;
import org.marid.ide.logging.IdeLogConsoleHandler;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.logging.Logs;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
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
@SpringBootApplication
@Import({IdeDependants.class})
@EnableScheduling
@PropertySource({"meta.properties", "ide.properties"})
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
    @Scope(SCOPE_PROTOTYPE)
    public Logs logs(InjectionPoint injectionPoint) {
        final Logger logger = Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
        return () -> logger;
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
