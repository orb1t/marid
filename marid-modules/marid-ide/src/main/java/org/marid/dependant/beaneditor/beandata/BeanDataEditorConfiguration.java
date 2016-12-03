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

package org.marid.dependant.beaneditor.beandata;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.MaridToolbar;
import org.marid.spring.xml.BeanArg;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.Map;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BeanDataActions.class, BeanDataDetails.class})
public class BeanDataEditorConfiguration {

    public BeanData beanData;

    @Bean
    public BeanData beanData() {
        return beanData;
    }

    @Bean
    public RefValuesEditor<BeanArg> beanArgsEditor(BeanData beanData, ProjectProfile profile) {
        return new RefValuesEditor<>(beanData.beanArgs, name -> profile.getArgType(beanData, name));
    }

    @Bean
    public RefValuesEditor<BeanProp> beanPropsEditor(BeanData beanData, ProjectProfile profile) {
        return new RefValuesEditor<>(beanData.properties, name -> profile.getPropType(beanData, name));
    }

    @Bean
    @Qualifier("beanData")
    @Order(1)
    public Tab beanArgsTab(RefValuesEditor<BeanArg> editor) {
        return new Tab(s("Constructor arguments"), new MaridScrollPane(editor));
    }

    @Bean
    @Qualifier("beanData")
    @Order(2)
    public Tab beanPropsTab(RefValuesEditor<BeanProp> editor) {
        return new Tab(s("Properties"), new MaridScrollPane(editor));
    }

    @Bean
    public TabPane beanDataEditorTabs(@Qualifier("beanData") Tab[] tabs) {
        final TabPane tabPane = new TabPane(tabs);
        tabPane.setTabClosingPolicy(UNAVAILABLE);
        return tabPane;
    }

    @Bean
    public ToolBar beanDataEditorToolbar(@Qualifier("beanData") Map<String, FxAction> actionMap) {
        return new MaridToolbar(actionMap);
    }

    @Bean
    public BorderPane sceneRoot(TabPane beanDataEditorTabs, ToolBar beanDataEditorToolbar) {
        return new BorderPane(beanDataEditorTabs, beanDataEditorToolbar, null, null, null);
    }

    @Bean
    public Scene simpleBeanConfigurerScene(BorderPane sceneRoot) {
        return new Scene(sceneRoot, 1024, 768);
    }

    @Bean(initMethod = "show")
    public Stage simpleBeanConfigurerStage(Scene simpleBeanConfigurerScene) {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setTitle(s("Bean editor"));
        stage.setScene(simpleBeanConfigurerScene);
        return stage;
    }
}
