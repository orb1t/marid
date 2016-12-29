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

package org.marid.editors.url;

import javafx.beans.value.WritableValue;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.marid.ide.project.ProfileInfo;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DValue;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class RelativeUrlEditor {

    @Bean
    public FileChooser chooser(ProfileInfo profile, List<ExtensionFilter> filters) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(profile.getSrcMainResources().toFile());
        fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser;
    }

    @Bean
    public ApplicationListener<ContextStartedEvent> startListener(FileChooser chooser, WritableValue<DElement<?>> value) {
        final Path basePath = chooser.getInitialDirectory().toPath();
        return new ApplicationListener<ContextStartedEvent>() {
            @Override
            public void onApplicationEvent(ContextStartedEvent event) {
                final GenericApplicationContext context = (GenericApplicationContext) event.getApplicationContext();
                context.getApplicationListeners().remove(this);
                try {
                    final File file = chooser.showOpenDialog(null);
                    if (file != null && file.toPath().startsWith(basePath)) {
                        final Path relative = basePath.relativize(file.toPath());
                        value.setValue(new DValue(relative.toString()));
                    }
                } finally {
                    if (context.isActive()) {
                        context.close();
                    }
                }
            }
        };
    }
}