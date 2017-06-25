/*
 *
 */

package org.marid.ide.structure.editor;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.awt.Desktop.Action.OPEN;
import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class DefaultFileEditor extends AbstractFileEditor<Desktop> {

    private final SpecialAction editAction;

    @Autowired
    public DefaultFileEditor(SpecialAction editAction) {
        super(Files::isRegularFile);
        this.editAction = editAction;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Open a file in a default editor";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("M_OPEN_IN_BROWSER");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Nullable
    @Override
    protected Desktop editorContext(@Nonnull Path path) {
        return isDesktopSupported() && getDesktop().isSupported(OPEN) ? getDesktop() : null;
    }

    @Override
    protected void edit(@Nonnull Path path, @Nonnull Desktop context) {
        EventQueue.invokeLater(() -> {
            try {
                context.open(path.toFile());
            } catch (Exception e) {
                n(WARNING, "Unable to edit {0}", e, path);
            }
        });
    }

    @Nullable
    @Override
    public SpecialAction getSpecialAction() {
        return editAction;
    }
}
