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

package org.marid.dependant.beaneditor;

import javafx.collections.ListChangeListener;
import javafx.util.Pair;
import org.marid.dependant.beaneditor.valuemenu.ValueMenuItems;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BeanEditorTab.class, BeanListConfiguration.class, ValueMenuItems.class})
public class BeanEditorConfiguration {

    public Path beanFilePath;
    public ProjectProfile profile;

    @Bean
    public Path beanFilePath() {
        return beanFilePath;
    }

    @Bean
    public ProjectProfile profile() {
        return profile;
    }

    @Bean
    public BeanFile beanFile(Path beanFilePath, ProjectProfile profile) {
        return profile.getBeanFiles()
                .stream()
                .filter(e -> e.getKey().equals(beanFilePath))
                .map(Pair::getValue)
                .findAny()
                .orElse(null);
    }

    @Autowired
    private void listenBeans(ProjectProfile profile, BeanEditorTab tab, Path beanFilePath) {
        final ListChangeListener<Pair<Path, BeanFile>> changeListener = c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(e -> {
                        if (e.getKey().equals(beanFilePath)) {
                            tab.getTabPane().getTabs().remove(tab);
                        }
                    });
                }
            }
        };
        profile.getBeanFiles().addListener(changeListener);
        tab.setOnCloseRequest(event -> profile.getBeanFiles().removeListener(changeListener));
    }
}
