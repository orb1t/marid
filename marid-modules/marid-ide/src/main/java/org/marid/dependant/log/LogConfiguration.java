/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.dependant.log;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.IdePrefs;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.annotation.Q;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.marid.dependant.log.LoggingTable.icon;
import static org.marid.jfx.icons.FontIcon.M_BORDER_TOP;
import static org.marid.jfx.icons.FontIcon.M_CLEAR_ALL;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
@ComponentScan(basePackageClasses = {LogConfiguration.class})
public class LogConfiguration {

    @Bean
    @Q(LogConfiguration.class)
    @Order(1)
    public Menu filterMenu(LoggingFilter loggingFilter) {
        final Menu menu = new Menu(s("Filter"));
        ReflectionUtils.doWithFields(Level.class, field -> {
            final Level level = (Level) field.get(null);
            final String name = level.getLocalizedName();
            final CheckMenuItem menuItem = new CheckMenuItem(name, glyphIcon(icon(level).icon, 16));
            menuItem.selectedProperty().bindBidirectional(loggingFilter.getProperty(level));
            menu.getItems().add(menuItem);
        }, field -> isStatic(field.getModifiers()) && isPublic(field.getModifiers()) && field.getType() == Level.class);
        return menu;
    }

    @Bean
    @Q(LogConfiguration.class)
    @Order(2)
    public Menu actionsMenu(LoggingFilter loggingFilter) {
        final Menu menu = new Menu(s("Actions"));
        {
            final MenuItem menuItem = new MenuItem(s("Clear"), glyphIcon(M_CLEAR_ALL, 16));
            menuItem.setOnAction(event -> loggingFilter.clear());
            menu.getItems().add(menuItem);
        }
        return menu;
    }

    @Bean
    @Q(LogConfiguration.class)
    public MenuBar logMenuBar(@Q(LogConfiguration.class) List<Menu> menus) {
        return new MenuBar(menus.toArray(new Menu[menus.size()]));
    }

    @Bean
    @Q(LogConfiguration.class)
    public BorderPane logPane(@Q(LogConfiguration.class) MenuBar menuBar, LoggingTable table) {
        return new BorderPane(new MaridScrollPane(table), menuBar, null, null, null);
    }

    @Bean(initMethod = "show")
    @Q(LogConfiguration.class)
    public Stage logStage(@Q(LogConfiguration.class) BorderPane logPane,
                          @Q(LogConfiguration.class) Menu actionsMenu) {
        final Preferences preferences = IdePrefs.PREFERENCES.node("logs");
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(logPane, preferences.getDouble("width", 800), preferences.getDouble("height", 600)));
        stage.setOnCloseRequest(event -> {
            preferences.putDouble("x", stage.getX());
            preferences.putDouble("y", stage.getY());
            preferences.putDouble("width", stage.getWidth());
            preferences.putDouble("height", stage.getHeight());
        });
        stage.setX(preferences.getDouble("x", stage.getX()));
        stage.setY(preferences.getDouble("y", stage.getY()));
        {
            actionsMenu.getItems().add(new SeparatorMenuItem());
            final CheckMenuItem menuItem = new CheckMenuItem(s("Always on top"), glyphIcon(M_BORDER_TOP, 16));
            menuItem.setOnAction(event -> stage.setAlwaysOnTop(!stage.isAlwaysOnTop()));
            actionsMenu.getItems().add(menuItem);
        }
        return stage;
    }
}
