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

package org.marid.dependant.beaneditor.propeditor;

import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.DProps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
@Import({PropActions.class, PropTable.class})
public class PropEditorConfiguration extends DependantConfiguration<PropEditorParams> {

    @Bean
    public ToolBar propEditorToolbar(PropTable propTable, PropActions propActions) {
        return new ToolbarBuilder()
                .add("Add property", FontIcon.M_ADD, propActions::onAdd)
                .build();
    }

    @Bean
    public BorderPane propEditorPane(PropTable propTable, ToolBar propEditorToolbar) {
        return new BorderPane(propTable, propEditorToolbar, null, null, null);
    }

    @Bean(initMethod = "show")
    public Stage propEditorStage(BorderPane propEditorPane) {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setTitle(s("Properties"));
        stage.setScene(new Scene(propEditorPane, 800, 600));
        return stage;
    }

    @Bean
    public DProps props() {
        return param.props;
    }
}
