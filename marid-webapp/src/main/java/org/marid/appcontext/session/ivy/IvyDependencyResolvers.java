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

package org.marid.appcontext.session.ivy;

import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;

@Component
public class IvyDependencyResolvers {

  private final LinkedList<DependencyResolver> resolvers = new LinkedList<>();

  public IvyDependencyResolvers(DependencyResolver[] resolvers) {
    Collections.addAll(this.resolvers, resolvers);
  }

  public void configure(IvySettings settings) {
    for (final DependencyResolver resolver : resolvers) {
      settings.addResolver(resolver);
    }
    settings.setDefaultResolver("central");
  }
}
