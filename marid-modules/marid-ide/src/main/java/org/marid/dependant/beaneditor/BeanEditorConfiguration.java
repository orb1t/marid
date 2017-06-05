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

import javafx.application.Platform;
import org.marid.ide.event.TextFileRemovedEvent;
import org.marid.ide.event.TextFileRenamedEvent;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParam> {

    @Bean
    public TextFile javaFile() {
        return param.javaFile;
    }

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public ApplicationListener<TextFileRemovedEvent> removeListener(TextFile javaFile, GenericApplicationContext ctx) {
        return event -> {
            if (javaFile.getPath().equals(event.getSource())) {
                ctx.close();
            }
        };
    }

    @Bean
    private ApplicationListener<TextFileRenamedEvent> renameListener(TextFile file) {
        return event -> {
            if (file.getPath().equals(event.getSource())) {
                Platform.runLater(() -> file.path.set(event.getTarget()));
            }
        };
    }
}
