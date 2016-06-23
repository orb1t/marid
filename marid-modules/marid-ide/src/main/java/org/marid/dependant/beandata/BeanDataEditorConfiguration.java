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

package org.marid.dependant.beandata;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.dependant.beaneditor.BeanEditorTable;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;
import static org.marid.jfx.ScrollPanes.scrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan(basePackageClasses = {BeanDataEditorConfiguration.class})
public class BeanDataEditorConfiguration implements L10nSupport {

    @Bean
    public BeanData beanData(BeanEditorTable beanEditorTable) {
        return beanEditorTable.getSelectionModel().getSelectedItem();
    }

    @Bean
    public RefValuesEditor<ConstructorArg> constructorArgEditor(BeanData beanData) {
        return new RefValuesEditor<>(beanData.constructorArgs);
    }

    @Bean
    public RefValuesEditor<Property> propertyEditor(BeanData beanData) {
        return new RefValuesEditor<>(beanData.properties);
    }

    @Bean
    public TabPane tabPane(RefValuesEditor<ConstructorArg> constructorArgEditor,
                           RefValuesEditor<Property> propertyEditor) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Constructor arguments"), scrollPane(constructorArgEditor)),
                new Tab(s("Properties"), scrollPane(propertyEditor))
        );
        tabPane.setTabClosingPolicy(UNAVAILABLE);
        return tabPane;
    }

    @Bean
    public Scene simpleBeanConfigurerScene(TabPane tabPane) {
        return new Scene(tabPane, 1024, 768);
    }

    @Bean
    public Stage simpleBeanConfigurerStage(IdePane idePane, Scene simpleBeanConfigurerScene) {
        final Stage stage = new Stage(StageStyle.UTILITY);
        stage.initOwner(idePane.getScene().getWindow());
        stage.setScene(simpleBeanConfigurerScene);
        return stage;
    }

    @Bean
    public ApplicationListener<ContextStartedEvent> contextStartedListener(Stage simpleBeanConfigurerStage) {
        return event -> simpleBeanConfigurerStage.show();
    }
}
