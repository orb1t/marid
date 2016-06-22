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

package org.marid.dependant.iconviewer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.jfx.ScrollPanes;
import org.marid.l10n.L10nSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IconViewer extends Stage implements L10nSupport {

    @Autowired
    public IconViewer(IconViewerTable table, IdePane idePane) {
        super(StageStyle.UTILITY);
        initOwner(idePane.getScene().getWindow());
        setTitle(s("Icon viewer"));
        final ScrollPane scrollPane = ScrollPanes.scrollPane(table);
        final BorderPane pane = new BorderPane(scrollPane);
        pane.setPrefSize(800, 600);
        final Label countLabel = new Label(s("Icon count: %d", table.getItems().size()));
        pane.setBottom(countLabel);
        BorderPane.setMargin(scrollPane, new Insets(10, 10, 5, 10));
        BorderPane.setMargin(countLabel, new Insets(5, 10, 10, 10));
        setScene(new Scene(pane));
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
