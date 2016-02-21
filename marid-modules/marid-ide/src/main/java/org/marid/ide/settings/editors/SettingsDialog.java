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

package org.marid.ide.settings.editors;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.marid.ide.scenes.IdeScene;
import org.marid.ide.settings.SettingsHolder;
import org.marid.ide.settings.SettingsManager;
import org.marid.l10n.L10nSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class SettingsDialog extends Dialog<SettingsHolder> implements L10nSupport {

    @Inject
    public SettingsDialog(IdeScene ideScene, SettingsManager settingsManager) {
        final SettingsHolder settingsHolder = new SettingsHolder(settingsManager.preferences());
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefSize(800, 600);
        dialogPane.setContent(tabPane(settingsHolder));
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
        setTitle(s("IDE settings"));
        initModality(Modality.WINDOW_MODAL);
        initOwner(ideScene.getWindow());
        setResizable(true);
        setResultConverter(type -> type == ButtonType.APPLY ? settingsHolder : null);
    }

    private TabPane tabPane(SettingsHolder settingsHolder) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Common"), new CommonTab(settingsHolder)),
                new Tab("Java", new JavaTab(settingsHolder))
        );
        for (final Tab tab : tabPane.getTabs()) {
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }
}
