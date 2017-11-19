/*-
 * #%L
 * marid-ide
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

package org.marid.ide.tabs;

import javafx.scene.control.Tab;
import org.marid.jfx.props.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PreDestroy;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeTab extends Tab {

  @Autowired
  private void init(GenericApplicationContext context, IdeTabPane tabPane) {
    tabPane.getTabs().add(this);
    tabPane.getSelectionModel().select(this);
    Props.addHandler(onClosedProperty(), event -> context.close());
  }

  @PreDestroy
  private void destroy() {
    Optional.ofNullable(getTabPane()).ifPresent(tabPane -> tabPane.getTabs().remove(this));
  }
}
