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

package org.marid.ide.structure.editor;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marid.ide.structure.syneditor.SynEditor;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SynFileEditor extends AbstractFileEditor<Path> {

    private final ObjectProvider<SynEditor> synEditorFactory;
    private final Map<SynStage, Path> pathMap = new WeakHashMap<>();
    private final SpecialAction editAction;

    @Autowired
    public SynFileEditor(@Qualifier("syn") PathMatcher synEditorMatcher,
                         ObjectProvider<SynEditor> synEditorFactory,
                         SpecialAction editAction) {
        super(synEditorMatcher);
        this.synEditorFactory = synEditorFactory;
        this.editAction = editAction;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Edit file";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("F_FILE_TEXT_ALT");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Nullable
    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }

    @Override
    protected void edit(@Nonnull Path path, @Nonnull Path context) {
        final SynStage synStage = new SynStage(synEditorFactory.getObject());
        synStage.setTitle(path.toString());
        synStage.synEditor.setPath(path);
        synStage.synEditor.load();
        pathMap.put(synStage, path);
        synStage.show();
    }

    @Nullable
    @Override
    public SpecialAction getSpecialAction() {
        return editAction;
    }

    private static class SynStage extends Stage {

        private final SynEditor synEditor;

        private SynStage(SynEditor synEditor) {
            this.synEditor = synEditor;
            setScene(new Scene(synEditor, 1024, 768));
        }
    }
}
