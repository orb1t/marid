/*
 *
 */

package org.marid.runtime;

/*-
 * #%L
 * marid-runtime
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
