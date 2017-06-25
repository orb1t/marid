package org.marid.runtime;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Lazy(false)
public class MaridRuntime {

    @PostConstruct
    private void init() {
        log(INFO, "Marid Runtime Image {0}", Instant.now());
    }

    @Bean
    public static PropertyOverrideConfigurer propertyOverrideConfigurer() {
        final PropertyOverrideConfigurer configurer = new PropertyOverrideConfigurer();
        configurer.setFileEncoding("UTF-8");
        configurer.setIgnoreResourceNotFound(true);
        configurer.setLocation(new ClassPathResource("beans.properties"));
        return configurer;
    }
}
