/*
 *
 */
package org.marid.ide.common;

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

import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.SpecialAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class SpecialActionConfiguration {

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction editAction() {
        return new SpecialAction("edit", "ed", "Edit")
                .bindText("Edit")
                .setAccelerator(KeyCombination.valueOf("F4"))
                .setIcon("M_EDIT")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction addAction() {
        return new SpecialAction("mod", "mod", "Edit")
                .bindText("Add")
                .setAccelerator(KeyCombination.valueOf("Ctrl+I"))
                .setIcon("M_ADD_BOX")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction removeAction() {
        return new SpecialAction("mod", "mod", "Edit")
                .bindText("Remove")
                .setAccelerator(KeyCombination.valueOf("F8"))
                .setIcon("D_MINUS_BOX")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction cutAction() {
        return new SpecialAction("cp", "cp", "Edit")
                .bindText("Cut")
                .setAccelerator(KeyCombination.valueOf("Ctrl+X"))
                .setIcon("M_CONTENT_CUT")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction copyAction() {
        return new SpecialAction("cp", "cp", "Edit")
                .bindText("Copy")
                .setAccelerator(KeyCombination.valueOf("Ctrl+C"))
                .setIcon("M_CONTENT_COPY")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction pasteAction() {
        return new SpecialAction("cp", "cp", "Edit")
                .bindText("Paste")
                .setAccelerator(KeyCombination.valueOf("Ctrl+V"))
                .setIcon("M_CONTENT_PASTE")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction clearAllAction() {
        return new SpecialAction("mod", "mod", "Edit")
                .bindText("Clear All")
                .setIcon("M_CLEAR_ALL")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction renameAction() {
        return new SpecialAction("edit", "ed", "Edit")
                .bindText("Rename")
                .setAccelerator(KeyCombination.valueOf("Ctrl+R"))
                .setIcon("D_RENAME_BOX")
                .setDisabled(true);
    }

    @IdeAction
    @Qualifier("specialAction")
    public SpecialAction selectAllAction() {
        return new SpecialAction("sel", "sel", "Edit")
                .bindText("Select All")
                .setAccelerator(KeyCombination.valueOf("Ctrl+A"))
                .setIcon("D_SELECT_ALL")
                .setDisabled(true);
    }
}
