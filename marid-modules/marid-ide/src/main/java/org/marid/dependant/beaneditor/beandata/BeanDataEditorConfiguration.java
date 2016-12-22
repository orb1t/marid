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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.xml.BeanData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BeanDataDetails.class, BeanInfo.class, ConstructorList.class})
public class BeanDataEditorConfiguration {

    public BeanData data;

    @Bean
    public BeanData data() {
        return data;
    }

    @Bean
    public BeanPropEditor beanArgsEditor(BeanData beanData, ProjectProfile profile) {
        return new BeanPropEditor(beanData.beanArgs, name -> beanData.getArgType(profile, name));
    }

    @Bean
    public BeanPropEditor beanPropsEditor(BeanData beanData, ProjectProfile profile) {
        return new BeanPropEditor(beanData.properties, name -> beanData.getPropType(profile, name));
    }

    @Bean
    @Qualifier("beanData")
    @Order(1)
    public Tab beanArgsTab(BeanPropEditor beanArgsEditor, ConstructorList constructorList) {
        final Tab tab = new Tab();
        final BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new MaridScrollPane(beanArgsEditor));
        borderPane.setBottom(constructorList);
        tab.setContent(borderPane);
        tab.textProperty().bind(ls("Parameters"));
        return tab;
    }

    @Bean
    @Qualifier("beanData")
    @Order(2)
    public Tab beanPropsTab(BeanPropEditor beanPropsEditor) {
        final Tab tab = new Tab();
        tab.setContent(new MaridScrollPane(beanPropsEditor));
        tab.textProperty().bind(ls("Properties"));
        return tab;
    }

    @Bean
    public TabPane beanDataEditorTabs(@Qualifier("beanData") Tab[] tabs) {
        final TabPane tabPane = new TabPane(tabs);
        tabPane.setTabClosingPolicy(UNAVAILABLE);
        return tabPane;
    }

    @Bean(initMethod = "show")
    public Stage simpleBeanConfigurerStage(TabPane beanDataEditorTabs) {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(beanDataEditorTabs, 1024, 768));
        stage.titleProperty().bind(ls("Bean editor"));
        return stage;
    }
}
