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

package org.marid.dependant.beaneditor.listeditor;

import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.Ide;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.list.MaridListActions;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.DCollection;
import org.marid.spring.xml.DValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
@Import({ListEditor.class})
public class ListEditorConfiguration extends DependantConfiguration<ListEditorParams> {

    @Bean
    public DCollection<?> collection() {
        return param.collection;
    }

    @Bean
    public ResolvableType type() {
        return param.type;
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction clearAction(ListEditor editor) {
        return MaridListActions.clearAction(editor);
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction removeAction(ListEditor editor) {
        return MaridListActions.removeAction(editor);
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction upAction(ListEditor editor) {
        return MaridListActions.upAction(editor);
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction downAction(ListEditor editor) {
        return MaridListActions.downAction(editor);
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction addAction(ListEditor editor) {
        return MaridListActions.addAction("Add", event -> editor.getItems().add(new DValue("#{null}")));
    }

    @Bean
    @Qualifier("listEditor")
    public ToolBar listEditorToolbar(@Qualifier("listEditor") Map<String, FxAction> actionMap) {
        return new ToolBar(MaridActions.toolbar(actionMap));
    }

    @Bean
    @Qualifier("listEditor")
    public BorderPane pane(@Qualifier("listEditor") ToolBar toolBar, ListEditor listEditor) {
        return new BorderPane(new MaridScrollPane(listEditor), toolBar, null, null, null);
    }

    @Bean(initMethod = "show")
    @Qualifier("listEditor")
    public Stage stage(@Qualifier("listEditor") BorderPane pane) {
        final Stage stage = new Stage(StageStyle.DECORATED);
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(pane, 800, 800));
        return stage;
    }
}
