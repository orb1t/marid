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

package org.marid.dependant.beaneditor.beans;

import javafx.collections.MapChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.marid.dependant.beaneditor.beans.beans.BeanListConfiguration;
import org.marid.dependant.beaneditor.beans.constants.ConstantListConfiguration;
import org.marid.dependant.beaneditor.beans.properties.PropertiesConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.providers.BeanDataProvider;
import org.marid.spring.xml.providers.ConstantsProvider;
import org.marid.spring.xml.providers.PropertiesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.nio.file.Path;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({
        BeanEditorTab.class,
        PropertiesConfiguration.class,
        ConstantListConfiguration.class,
        BeanListConfiguration.class
})
public class BeanEditorConfiguration {

    @Bean
    public ProjectProfile projectProfile(Environment environment) {
        return environment.getProperty("profile", ProjectProfile.class);
    }

    @Bean
    public Path beanFilePath(Environment environment) {
        return environment.getProperty("beanFilePath", Path.class);
    }

    @Bean
    public BeanFile beanFile(Path beanFilePath, ProjectProfile profile) {
        return profile.getBeanFiles().get(beanFilePath);
    }

    @Bean
    public PropertiesProvider propertiesProvider(BeanFile beanFile) {
        return () -> beanFile.properties;
    }

    @Bean
    public ConstantsProvider constantsProvider(BeanFile beanFile) {
        return () -> beanFile.constants;
    }

    @Bean
    public BeanDataProvider beanDataProvider(BeanFile beanFile) {
        return () -> beanFile.beans;
    }

    @Autowired
    private void listenBeans(ProjectProfile profile, BeanEditorTab tab, Path beanFilePath) {
        final MapChangeListener<Path, BeanFile> changeListener = change -> {
            if (change.wasRemoved()) {
                if (beanFilePath.equals(change.getKey())) {
                    tab.getTabPane().getTabs().remove(tab);
                }
            }
        };
        profile.getBeanFiles().addListener(changeListener);
        tab.setOnCloseRequest(event -> profile.getBeanFiles().removeListener(changeListener));
    }

    @Bean
    public TabPane beanEditorTabs(BorderPane beanEditor,
                                  BorderPane constantsEditor,
                                  BorderPane propertiesEditor) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Beans"), beanEditor),
                new Tab(s("Constants"), constantsEditor),
                new Tab(s("Properties"), propertiesEditor),
                new Tab(s("Maps"), new BorderPane()),
                new Tab(s("Lists"), new BorderPane()),
                new Tab(s("Sets"), new BorderPane())
        );
        tabPane.setSide(Side.BOTTOM);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }
}
