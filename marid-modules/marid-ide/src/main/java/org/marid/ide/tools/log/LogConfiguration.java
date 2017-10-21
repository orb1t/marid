/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.tools.log;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.marid.spring.ui.FxBean;
import org.marid.spring.ui.FxComponent;
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
