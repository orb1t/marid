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

package org.marid.dependant.beaneditor;

import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParam> {

    @Bean
    public Path javaFile() {
        return param.javaFile;
    }

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }
}
