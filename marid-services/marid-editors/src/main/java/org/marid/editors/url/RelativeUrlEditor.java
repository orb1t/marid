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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public FileChooser chooser(ProfileInfo profileInfo, List<ExtensionFilter> filters) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(profileInfo.getSrcMainResources().toFile());
        fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser;
    }

    @Autowired
    public void init(FileChooser fileChooser, WritableValue<DElement<?>> value) {
        final Path basePath = fileChooser.getInitialDirectory().toPath();
        final File file = fileChooser.showOpenDialog(null);
        if (file != null && file.toPath().startsWith(basePath)) {
            final Path relative = basePath.relativize(file.toPath());
            value.setValue(new DValue(relative.toString()));
        }
    }
}