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

package org.marid.app.config;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.*;
import org.marid.app.annotation.Handler;
import org.marid.app.annotation.HandlerQualifier;
import org.marid.app.props.UndertowProperties;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.function.BiConsumer;

@Configuration
@EnableConfigurationProperties(UndertowProperties.class)
public class UndertowConfiguration {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Undertow undertow(SSLContext sslContext, UndertowProperties properties, HttpHandler handler) {
    return Undertow.builder()
        .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
        .setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true)
        .addHttpsListener(properties.getPort(), properties.getHost(), sslContext, handler)
        .build();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public SessionManager sessionManager(UndertowProperties properties) {
    final InMemorySessionManager sessionManager = new InMemorySessionManager("maridSessionManager");
    sessionManager.setDefaultSessionTimeout(properties.getSessionTimeout());
    return sessionManager;
  }

  @Bean
  public SessionConfig sessionConfig() {
    final SessionCookieConfig cookieConfig = new SessionCookieConfig();
    cookieConfig.setCookieName("sid");
    cookieConfig.setHttpOnly(true);
    cookieConfig.setSecure(true);
    return cookieConfig;
  }

  @Bean
  public PathHandler rootHandler(@HandlerQualifier Map<String, HttpHandler> handlers, GenericApplicationContext ctx) {
    final PathHandler pathHandler = new PathHandler();
    final BiConsumer<AnnotatedTypeMetadata, HttpHandler> processor = (metadata, handler) -> {
      if (metadata == null) {
        return;
      }
      final Map<String, Object> values = metadata.getAnnotationAttributes(Handler.class.getName());
      if (values != null) {
        final String path = (String) values.get("path");
        final Boolean exact = (Boolean) values.get("exact");
        if (path != null && exact != null) {
          if (exact) {
            pathHandler.addExactPath(path, handler);
          } else {
            pathHandler.addPrefixPath(path, handler);
          }
        }
      }
    };

    handlers.forEach((name, handler) -> {
      final BeanDefinition definition = ctx.getBeanDefinition(name);
      if (definition instanceof AnnotatedBeanDefinition) {
        final AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) definition;
        processor.accept(abd.getMetadata(), handler);
        processor.accept(abd.getFactoryMethodMetadata(), handler);
      }
    });

    return pathHandler;
  }

  @Bean
  public SessionAttachmentHandler handler(PathHandler rootHandler, SessionManager sessionManager, SessionConfig config) {
    return new SessionAttachmentHandler(rootHandler, sessionManager, config);
  }
}

