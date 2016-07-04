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

package org.marid.dependant.settings;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.settings.AbstractSettings;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SettingsDialog extends Dialog<ButtonType> {

    @Autowired
    public SettingsDialog(IdePane idePane, List<SettingsEditor> editors, AnnotationConfigApplicationContext context) {
        final DialogPane dialogPane = getDialogPane();
        setOnHidden(event -> context.close());
        dialogPane.setPrefSize(800, 600);
        dialogPane.setContent(tabPane(editors));
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        setTitle(L10n.s("IDE settings"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(idePane.getScene().getWindow());
        setResizable(true);
        final Map<AbstractSettings, byte[]> snapshot = editors.stream()
                .collect(toMap(SettingsEditor::getSettings, e -> e.getSettings().save()));
        setResultConverter(param -> {
            switch (param.getButtonData()) {
                case CANCEL_CLOSE:
                    snapshot.forEach(AbstractSettings::load);
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
                .map(editor -> new Tab(L10n.s(editor.getSettings().getName()), editor.getNode()))
                .toArray(Tab[]::new));
        for (final Tab tab : tabPane.getTabs()) {
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }
}
