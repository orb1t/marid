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
package org.marid.ui.webide.base;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;
import org.marid.applib.l10n.Strs;
import org.marid.applib.spring.init.Init;
import org.marid.applib.spring.init.Inits;
import org.springframework.stereotype.Component;

@Component
public class MainMenuBar extends MenuBar implements Inits {

  private final MenuItem sessionItem;

  public MainMenuBar(Strs strs) {
    sessionItem = addItem(strs.s("session"), VaadinIcons.USER, null);
    setWidth(100, Unit.PERCENTAGE);
    setHeight(-1, Unit.PIXELS);
  }

  @Init(1)
  public void logout(Strs strs) {
    sessionItem.addItem(strs.s("logout"), VaadinIcons.EXIT, item -> {
      getSession().close();
      getUI().getPage().setLocation("/logout");
    });
  }

  public MenuItem getSessionItem() {
    return sessionItem;
  }
}
