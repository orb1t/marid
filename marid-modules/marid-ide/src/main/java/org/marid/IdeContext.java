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

package org.marid;

import org.marid.ide.IdePostProcessor;
import org.marid.ide.logging.IdeLogHandler;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.DefaultLifecycleProcessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import static org.springframework.context.support.AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@EnableScheduling
@PropertySource("meta.properties")
@Import({IdePostProcessor.class, IdeDependants.class})
@ComponentScan(basePackages = {"org.marid.ide"}, lazyInit = true)
public class IdeContext {

    @Bean
    public Ide ide(AnnotationConfigApplicationContext context) {
        return context.getEnvironment().getProperty("ide", Ide.class);
    }

    @Bean
    public IdeLogHandler ideLogHandler() {
        return Stream.of(Logger.getLogger("").getHandlers())
                .filter(IdeLogHandler.class::isInstance)
                .map(IdeLogHandler.class::cast)
                .findAny()
                .orElse(null);
    }

    @Bean(name = LIFECYCLE_PROCESSOR_BEAN_NAME)
    public LifecycleProcessor lifecycleProcessor() {
        return new DefaultLifecycleProcessor();
    }

    @Bean
    public ConcurrentTaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(new ScheduledThreadPoolExecutor(1));
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> taskSchedulerDestroyer(ConcurrentTaskScheduler taskScheduler, Ide ide) {
        return event -> {
            if (event.getApplicationContext() == ide.context) {
                ((ScheduledThreadPoolExecutor) taskScheduler.getConcurrentExecutor()).shutdown();
            }
        };
    }

    @Bean
    @Scope("prototype")
    public Preferences preferences(InjectionPoint injectionPoint,
                                   @Value("${implementation.version}") String version) {
        final Class<?> type = injectionPoint.getMember().getDeclaringClass();
        return Preferences.userNodeForPackage(type).node(type.getName()).node(version);
    }
}
