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

package org.marid.ide.tools.log;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.marid.idelib.spring.ui.FxBean;
import org.marid.idelib.spring.ui.FxComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.marid.ide.tools.log.LoggingTable.icon;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@FxComponent
public class LogConfiguration {

  @Qualifier("log")
  @FxBean
  @Order(1)
  public Menu filterMenu(LoggingFilter loggingFilter) {
    final Menu menu = new Menu(s("Filter"));
    Stream.of(
        Level.ALL,
        Level.CONFIG,
        Level.FINE,
        Level.FINER,
        Level.FINEST,
        Level.INFO,
        Level.SEVERE,
        Level.WARNING,
        Level.OFF
    ).sorted(Comparator.comparingInt(Level::intValue)).forEach(level -> {
      final String name = level.getLocalizedName();
      final CheckMenuItem menuItem = new CheckMenuItem(name, glyphIcon(icon(level).icon, 16));
      menuItem.selectedProperty().bindBidirectional(loggingFilter.getProperty(level));
      menu.getItems().add(menuItem);
    });
    return menu;
  }

  @Qualifier("log")
  @FxBean
  @Order(2)
  public Menu actionsMenu(LoggingFilter loggingFilter) {
    final Menu menu = new Menu(s("Actions"));
    {
      final MenuItem menuItem = new MenuItem(s("Clear"), glyphIcon("M_CLEAR_ALL", 16));
      menuItem.setOnAction(event -> loggingFilter.clear());
      menu.getItems().add(menuItem);
    }
    return menu;
  }

  @Qualifier("log")
  @FxBean
  public MenuBar logMenuBar(@Qualifier("log") List<Menu> menus) {
    return new MenuBar(menus.toArray(new Menu[menus.size()]));
  }

  @Qualifier("log")
  @FxBean
  public BorderPane logPane(@Qualifier("log") MenuBar menuBar, LoggingTable table) {
    return new BorderPane(table, menuBar, null, null, null);
  }
}
