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
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ScrollPane;
import org.marid.ee.IdeSingleton;
import org.marid.ide.icons.IdeIcons;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class LoggingPane extends ScrollPane {

    private final LoggingFilter loggingFilter;

    @Inject
    public LoggingPane(LoggingFilter loggingFilter, LoggingTable table) {
        super(table);
        this.loggingFilter = loggingFilter;
        setFitToHeight(true);
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent");
    }

    @Produces
    @IdeMenuItem(menu = "Log", text = "Clear all log records", group = "clear", mIcons = {MaterialIcon.CLEAR_ALL}, key = "F7")
    @IdeToolbarItem(group = "log")
    public EventHandler<ActionEvent> clearLog(LoggingFilter loggingFilter) {
        return event -> loggingFilter.clear();
    }

    private CheckMenuItem menuItem(Level level) {
        final LoggingTable.IconDescriptor iconDescriptor = LoggingTable.icon(level);
        final GlyphIcon<?> glyphIcon = IdeIcons.glyphIcon(iconDescriptor.icon, 16);
        final CheckMenuItem menuItem = new CheckMenuItem(level.getLocalizedName(), glyphIcon);
        menuItem.selectedProperty().bindBidirectional(loggingFilter.getProperty(level));
        return menuItem;
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "02")
    public CheckMenuItem offMenuItem() {
        return menuItem(Level.OFF);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "02")
    public CheckMenuItem severeMenuItem() {
        return menuItem(Level.SEVERE);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "03")
    public CheckMenuItem warningMenuItem() {
        return menuItem(Level.WARNING);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "04")
    public CheckMenuItem infoMenuItem() {
        return menuItem(Level.INFO);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "05")
    public CheckMenuItem configMenuItem() {
        return menuItem(Level.CONFIG);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "06")
    public CheckMenuItem fineMenuItem() {
        return menuItem(Level.FINE);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "07")
    public CheckMenuItem finerMenuItem() {
        return menuItem(Level.FINER);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levels", text = "08")
    public CheckMenuItem finestMenuItem() {
        return menuItem(Level.FINEST);
    }

    @Produces
    @IdeMenuItem(menu = "Log", group = "levelsAll", text = "09")
    public CheckMenuItem allLevelsMenuItem() {
        return menuItem(Level.ALL);
    }
}