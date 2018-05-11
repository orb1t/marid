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
package org.marid.app.templates;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Component
public class ThymeleafConfiguration {

  @Bean
  public ClassLoaderTemplateResolver htmlTemplateResolver() {
    final var resolver = new ClassLoaderTemplateResolver(Thread.currentThread().getContextClassLoader());
    resolver.setPrefix("/templates/");
    resolver.setSuffix(".html");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setCacheable(true);
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setName("html");
    return resolver;
  }

  @Bean
  public TemplateEngine htmlTemplateEngine(ClassLoaderTemplateResolver htmlTemplateResolver) {
    final var templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(htmlTemplateResolver);
    return templateEngine;
  }
}
