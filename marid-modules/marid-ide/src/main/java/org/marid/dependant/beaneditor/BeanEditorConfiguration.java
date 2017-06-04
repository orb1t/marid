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

import org.marid.ide.event.FileRemovedEvent;
import org.marid.ide.event.FileRenamedEvent;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParam> {

    @Bean
    public Path javaFile() {
        return param.javaFile;
    }

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public ApplicationListener<FileRenamedEvent> renameListener(Path javaFile, GenericApplicationContext context) {
        return event -> {
            if (javaFile.equals(event.getSource())) {
                context.close();
            }
        };
    }

    @Bean
    public ApplicationListener<FileRemovedEvent> removeListener(Path javaFile, GenericApplicationContext context) {
        return event -> {
            if (javaFile.equals(event.getSource())) {
                context.close();
            }
        };
    }
}
