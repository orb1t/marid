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

package org.marid.dependant.monitor;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.marid.ide.tabs.IdeTab;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.O_GRAPH;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@EnableScheduling
@Import({ClassLoadingWidget.class, MemoryWidget.class, OperatingSystemWidget.class, ThreadWidget.class})
public class MonitorConfiguration {

    @Bean
    public GridPane monitorGridPane() {
        final GridPane pane = new GridPane();

        final ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().add(col1);

        final RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setVgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row1);

        final ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().add(col2);

        final RowConstraints row2 = new RowConstraints();
        row2.setFillHeight(true);
        row2.setVgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row2);
        return pane;
    }

    @Bean
    public IdeTab tab(GridPane monitorGridPane) {
        return new IdeTab(monitorGridPane, ls("Monitor"), () -> glyphIcon(O_GRAPH, 16));
    }
}
