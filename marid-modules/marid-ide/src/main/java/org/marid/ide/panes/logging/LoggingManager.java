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

package org.marid.ide.panes.logging;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;

import static org.marid.jfx.icons.FontIcon.M_CLEAR_ALL;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class LoggingManager {

    private final LoggingFilter loggingFilter;

    @Autowired
    public LoggingManager(LoggingFilter loggingFilter) {
        this.loggingFilter = loggingFilter;
    }

    @Bean
    @IdeMenuItem(menu = "Log", text = "Clear all log records", group = "clear", icon = M_CLEAR_ALL, key = "F7")
    @IdeToolbarItem(group = "log")
    public EventHandler<ActionEvent> clearLog(LoggingFilter loggingFilter) {
        return event -> loggingFilter.clear();
    }

    private CheckMenuItem menuItem(Level level) {
        final LoggingTable.IconDescriptor iconDescriptor = LoggingTable.icon(level);
        final GlyphIcon<?> glyphIcon = FontIcons.glyphIcon(iconDescriptor.icon, 16);
        final CheckMenuItem menuItem = new CheckMenuItem(level.getLocalizedName(), glyphIcon);
        menuItem.selectedProperty().bindBidirectional(loggingFilter.getProperty(level));
        return menuItem;
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "02")
    public CheckMenuItem offMenuItem() {
        return menuItem(Level.OFF);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "02")
    public CheckMenuItem severeMenuItem() {
        return menuItem(Level.SEVERE);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "03")
    public CheckMenuItem warningMenuItem() {
        return menuItem(Level.WARNING);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "04")
    public CheckMenuItem infoMenuItem() {
        return menuItem(Level.INFO);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "05")
    public CheckMenuItem configMenuItem() {
        return menuItem(Level.CONFIG);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "06")
    public CheckMenuItem fineMenuItem() {
        return menuItem(Level.FINE);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "07")
    public CheckMenuItem finerMenuItem() {
        return menuItem(Level.FINER);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levels", text = "08")
    public CheckMenuItem finestMenuItem() {
        return menuItem(Level.FINEST);
    }

    @Bean
    @IdeMenuItem(menu = "Log", group = "levelsAll", text = "09")
    public CheckMenuItem allLevelsMenuItem() {
        return menuItem(Level.ALL);
    }
}
