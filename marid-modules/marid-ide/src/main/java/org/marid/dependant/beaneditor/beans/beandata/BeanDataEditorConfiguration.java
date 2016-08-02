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

package org.marid.dependant.beaneditor.beans.beandata;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.MaridToolbar;
import org.marid.spring.annotation.TypeQualifier;
import org.marid.spring.xml.data.BeanData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.env.Environment;

import java.util.Map;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BeanDataActions.class, RefValuesEditorProvider.class})
public class BeanDataEditorConfiguration {

    @Bean
    public BeanData beanData(Environment environment) {
        return environment.getProperty("beanData", BeanData.class);
    }

    @Bean
    public TabPane tabPane(RefValuesEditorProvider provider, BeanData beanData) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Constructor arguments"), new MaridScrollPane(provider.newEditor(beanData.beanArgs))),
                new Tab(s("Properties"), new MaridScrollPane(provider.newEditor(beanData.properties)))
        );
        tabPane.setTabClosingPolicy(UNAVAILABLE);
        return tabPane;
    }

    @Bean
    public BorderPane sceneRoot(TabPane tabPane, @TypeQualifier(BeanDataActions.class) Map<String, FxAction> actionMap) {
        return new BorderPane(tabPane, new MaridToolbar(actionMap), null, null, null);
    }

    @Bean
    public Scene simpleBeanConfigurerScene(BorderPane sceneRoot) {
        return new Scene(sceneRoot, 1024, 768);
    }

    @Bean
    public Stage simpleBeanConfigurerStage(IdePane idePane, Scene simpleBeanConfigurerScene) {
        final Stage stage = new Stage(StageStyle.UTILITY);
        stage.initOwner(idePane.getScene().getWindow());
        stage.setTitle(s("Bean editor"));
        stage.setScene(simpleBeanConfigurerScene);
        return stage;
    }

    @Bean
    public ApplicationListener<ContextStartedEvent> contextStartedListener(Stage simpleBeanConfigurerStage) {
        return event -> simpleBeanConfigurerStage.show();
    }
}
