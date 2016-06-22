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

package org.marid.dependant.beanconf;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.marid.dependant.beanconf.editors.RefValueListEditor;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;
import static org.marid.jfx.ScrollPanes.scrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class SimpleBeanConfigurerContext implements L10nSupport {

    @Bean
    public RefValueListEditor<ConstructorArg> constructorArgEditor(ProjectProfile profile,
                                                                   ApplicationEventPublisher eventPublisher,
                                                                   BeanData beanData) {
        return new RefValueListEditor<>(profile, eventPublisher, beanData.constructorArgs);
    }

    @Bean
    public RefValueListEditor<Property> propertyEditor(ProjectProfile profile,
                                                       ApplicationEventPublisher eventPublisher,
                                                       BeanData beanData) {
        return new RefValueListEditor<>(profile, eventPublisher, beanData.properties);
    }

    @Bean
    public TabPane tabPane(RefValueListEditor<ConstructorArg> constructorArgEditor,
                           RefValueListEditor<Property> propertyEditor) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Constructor arguments"), scrollPane(constructorArgEditor)),
                new Tab(s("Properties"), scrollPane(propertyEditor))
        );
        tabPane.setTabClosingPolicy(UNAVAILABLE);
        return tabPane;
    }
}
