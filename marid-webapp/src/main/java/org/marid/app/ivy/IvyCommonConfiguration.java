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

package org.marid.app.ivy;

import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.marid.app.spring.M2RepositoryExists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
public class IvyCommonConfiguration {

  @Bean
  public IBiblioResolver iBiblioResolver() {
    final IBiblioResolver resolver = new IBiblioResolver();
    resolver.setM2compatible(true);
    resolver.setUsepoms(true);
    resolver.setName("central");
    return resolver;
  }

  @Bean
  @Conditional({M2RepositoryExists.class})
  public FileSystemResolver localMavenResolver() {
    final FileSystemResolver resolver = new FileSystemResolver();
    resolver.setLocal(true);
    resolver.setM2compatible(true);
    resolver.setName("local");
    return resolver;
  }
}
