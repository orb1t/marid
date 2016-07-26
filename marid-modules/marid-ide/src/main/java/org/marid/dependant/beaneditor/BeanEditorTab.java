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

import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.controls.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class BeanEditorTab extends IdeTab {

    public final ProjectProfile profile;
    public final Path beanFilePath;

    @Autowired
    public BeanEditorTab(ProjectProfile profile, BorderPane beanEditor, Path beanFilePath) {
        super(beanEditor, "[%s]: %s", profile, profile.getBeansDirectory().relativize(beanFilePath));
        this.profile = profile;
        this.beanFilePath = beanFilePath;
    }
}
