/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.dependant.settings;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.marid.Ide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SettingsDialog extends Dialog<ButtonType> {

    @Autowired
    public SettingsDialog(List<SettingsEditor> editors) {
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefSize(800, 600);
        dialogPane.setContent(tabPane(editors));
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE, ButtonType.APPLY);
        titleProperty().bind(ls("IDE settings"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(Ide.primaryStage);
        setResizable(true);
        setResultConverter(param -> {
            switch (param.getButtonData()) {
                case CANCEL_CLOSE:
                    return null;
                default:
                    return param;
            }
        });
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        showAndWait();
    }

    private TabPane tabPane(List<SettingsEditor> settingsEditors) {
        final TabPane tabPane = new TabPane(settingsEditors.stream()
                .sorted(Comparator.comparing(e -> e.getSettings().getName()))
                .map(editor -> {
                    final Tab tab = new Tab(null, editor.getNode());
                    tab.textProperty().bind(ls(editor.getSettings().getName()));
                    return tab;
                })
                .toArray(Tab[]::new));
        for (final Tab tab : tabPane.getTabs()) {
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }
}
