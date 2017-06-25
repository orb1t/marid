/*
 *
 */

package org.marid.dependant.iconviewer;

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

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IconViewer extends Stage {

    @Autowired
    public IconViewer(IconViewerTable table, IdePane idePane) {
        super(StageStyle.UNIFIED);
        initOwner(idePane.getScene().getWindow());
        setTitle(L10n.s("Icon viewer"));
        final BorderPane pane = new BorderPane(table);
        pane.setPrefSize(800, 600);
        final Label countLabel = new Label(L10n.s("Icon count: %d", table.getItems().size()));
        pane.setBottom(countLabel);
        BorderPane.setMargin(table, new Insets(10, 10, 5, 10));
        BorderPane.setMargin(countLabel, new Insets(5, 10, 10, 10));
        setScene(new Scene(pane));
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
