/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.web;

import org.marid.misc.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.OutputStream;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class SimpleWebServerTestConf {

    @Bean
    public HttpInterceptor interceptor() {
        return httpExchange -> {
            try {
                httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
                httpExchange.sendResponseHeaders(200, 0L);
                try (final OutputStream os = httpExchange.getResponseBody()) {
                    os.write("Hello".getBytes(UTF_8));
                }
            } finally {
                httpExchange.close();
            }
        };
    }

    @Bean
    public SimpleWebServerProperties webServerProperties(HttpInterceptor httpInterceptor) {
        return new Builder<>(new SimpleWebServerProperties())
                .set(SimpleWebServerProperties::setPort, 0)
                .set(SimpleWebServerProperties::setHandlerMap, Collections.singletonMap("/", httpInterceptor))
                .build();
    }

    @Bean
    public SimpleWebServer simpleWebServer(SimpleWebServerProperties webServerProperties) {
        return new SimpleWebServer(webServerProperties);
    }
}
