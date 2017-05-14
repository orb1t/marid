/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static org.marid.ide.common.IdeShapes.profileNode;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class BeanTreeConfiguration extends DependantConfiguration<BeanTreeParam> {

    @Bean
    public ProjectProfile profile() {
        final ProjectProfile profile = param.profile;
        profile.getBeanFiles().forEach(f -> f.beans.forEach(profile::updateBeanData));
        return profile;
    }

    @Bean
    public IdeTab tab(BeanTree tree, ProjectProfile profile) {
        final ObservableValue<String> text = new SimpleStringProperty(profile.getName());
        return new IdeTab(tree, text, () -> profileNode(profile, 16));
    }
}
