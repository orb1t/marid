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

import org.marid.ide.common.IdeValues;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.logging.Logs;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@EnableScheduling
@PropertySource({"meta.properties", "ide.properties"})
@Import({IdeDependants.class})
@ComponentScan(basePackages = {"org.marid.ide"}, lazyInit = true)
public class IdeContext {

    @Bean
    public Ide ide() {
        return Ide.ide;
    }

    @Bean
    public IdeLogHandler ideLogHandler() {
        return Ide.ideLogHandler;
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledThreadPoolExecutor scheduledExecutorService() {
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
}
