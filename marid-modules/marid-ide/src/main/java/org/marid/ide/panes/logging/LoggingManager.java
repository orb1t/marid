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

import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.MenuAction;
import org.marid.spring.action.ToolbarAction;
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
    @MenuAction
    @ToolbarAction
    public FxAction clearLogAction(LoggingFilter loggingFilter) {
        return new FxAction("log", "clear", "Log")
                .setText("Clear all log records")
                .setIcon(M_CLEAR_ALL)
                .setAccelerator(KeyCombination.valueOf("F7"))
                .setEventHandler(event -> loggingFilter.clear());
    }

    public FxAction levelAction(Level level) {
        final FxAction action = new FxAction(null, "levels", "Log")
                .setText(level.getLocalizedName())
                .setIcon(LoggingTable.icon(level).icon)
                .setSelected(false);
        action.selectedProperty().bindBidirectional(loggingFilter.getProperty(level));
        return action;
    }

    @Bean
    @MenuAction
    public FxAction offMenuItem() {
        return levelAction(Level.OFF);
    }

    @Bean
    @MenuAction
    public FxAction severeMenuItem() {
        return levelAction(Level.SEVERE);
    }

    @Bean
    @MenuAction
    public FxAction warningMenuItem() {
        return levelAction(Level.WARNING);
    }

    @Bean
    @MenuAction
    public FxAction infoMenuItem() {
        return levelAction(Level.INFO);
    }

    @Bean
    @MenuAction
    public FxAction configMenuItem() {
        return levelAction(Level.CONFIG);
    }

    @Bean
    @MenuAction
    public FxAction fineMenuItem() {
        return levelAction(Level.FINE);
    }

    @Bean
    @MenuAction
    public FxAction finerMenuItem() {
        return levelAction(Level.FINER);
    }

    @Bean
    @MenuAction
    public FxAction finestMenuItem() {
        return levelAction(Level.FINEST);
    }

    @Bean
    @MenuAction
    public FxAction allLevelsMenuItem() {
        return levelAction(Level.ALL);
    }
}
