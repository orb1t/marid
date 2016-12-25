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

import javafx.stage.FileChooser;
import org.marid.beans.Title;
import org.marid.ide.project.ProfileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@Title("Relative URL editor")
public class RelativeUrlEditor {

    @Autowired
    public void init(ProfileInfo profileInfo) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(profileInfo.getSrcMainResources().toFile());
        final File file = fileChooser.showOpenDialog(null);
        if (file != null) {

        }
    }
}
