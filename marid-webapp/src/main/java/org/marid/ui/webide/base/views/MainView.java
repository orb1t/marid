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
package org.marid.ui.webide.base.views;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import org.marid.applib.l10n.Strs;
import org.marid.applib.spring.Initializer;
import org.marid.applib.view.StaticView;
import org.marid.applib.view.ViewName;
import org.springframework.stereotype.Component;

@ViewName("")
@Component
public class MainView extends VerticalLayout implements StaticView {

  private final Accordion accordion = new Accordion();

  public MainView() {
    addComponent(accordion);
    setWidthUndefined();
    setComponentAlignment(accordion, Alignment.MIDDLE_CENTER);
  }

  @Initializer(order = 1)
  public Runnable sessionTab(Strs strs) {
    return () -> {
      final var buttons = new VerticalLayout();
      buttons.setWidth(100, Unit.PERCENTAGE);
      buttons.setDefaultComponentAlignment(Alignment.TOP_CENTER);

      {
        final var button = new Button("xxx");
        button.setWidth(100, Unit.PERCENTAGE);
        buttons.addComponent(button);
      }

      {
        final var button = new Button(strs.s("logout"), event -> {
          getSession().close();
          getUI().getPage().setLocation("/logout");
        });
        button.setWidth(100, Unit.PERCENTAGE);
        buttons.addComponent(button);
      }


      accordion.addTab(buttons, strs.s("sessions"));
    };
  }
}
