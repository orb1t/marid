package org.marid;

import org.marid.ide.common.IdeValues;
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
