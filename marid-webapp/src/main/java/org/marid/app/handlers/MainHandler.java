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

package org.marid.app.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import org.marid.app.annotation.Handler;
import org.marid.app.annotation.HandlerQualifier;
import org.marid.app.auth.MaridSecurityHandler;
import org.pac4j.core.config.Config;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MainHandler extends PathHandler {

  private final Config config;

  public MainHandler(Config config) {
    this.config = config;
  }

  @Autowired
  public void initPubResources(HttpHandler pubResourcesHandler) {
    addPrefixPath("/public", pubResourcesHandler);
  }

  @Autowired
  public void initPathHandlers(@HandlerQualifier Map<String, HttpHandler> handlers, GenericApplicationContext context) {
    handlers.forEach((name, handler) -> {
      final BeanDefinition definition = context.getBeanDefinition(name);
      if (definition instanceof AnnotatedBeanDefinition) {
        final AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) definition;
        process(abd.getMetadata(), handler);
        process(abd.getFactoryMethodMetadata(), handler);
      }
    });
  }

  private void process(AnnotatedTypeMetadata metadata, HttpHandler handler) {
    if (metadata != null) {
      final Map<String, Object> values = metadata.getAnnotationAttributes(Handler.class.getName());
      if (values != null) {
        final Handler h = AnnotationUtils.synthesizeAnnotation(values, Handler.class, null);

        if (h.secure()) {
          final String authorizer = h.authorizer().isEmpty() ? null : h.authorizer();
          final String client = h.client().isEmpty() ? null : h.client();
          handler = new MaridSecurityHandler(config, authorizer, client, handler, h.processUnauthorized());
        }

        if (h.exact()) {
          addExactPath(h.path(), handler);
        } else {
          addPrefixPath(h.path(), handler);
        }
      }
    }
  }
}
