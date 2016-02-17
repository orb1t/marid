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

package org.marid.ide.profile;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import org.apache.maven.model.Model;
import org.marid.ide.scenes.IdeScene;
import org.marid.pref.PrefSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectDataEditor extends Dialog<Model> implements PrefSupport {

    @Inject
    public ProjectDataEditor(IdeScene ideScene, ProjectDataEditorPane editorPane) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(ideScene.getWindow());
        setDialogPane(editorPane);
        setResultConverter(type -> type == ButtonType.APPLY ? editorPane.getModel() : null);
    }
}
