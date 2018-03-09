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

package org.marid.app.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.IBehaviorInstantiationListener;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

public class MaridComponentInjector implements IComponentInstantiationListener, IBehaviorInstantiationListener {

  private final AutowireCapableBeanFactory beanFactory;

  public MaridComponentInjector(ApplicationContext applicationContext) {
    this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
  }

  @Override
  public void onInstantiation(Behavior behavior) {
    beanFactory.autowireBean(behavior);
  }

  @Override
  public void onInstantiation(Component component) {
    beanFactory.autowireBean(component);
  }
}
