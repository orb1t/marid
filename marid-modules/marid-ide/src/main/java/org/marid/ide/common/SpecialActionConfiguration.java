package org.marid.ide.common;

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
