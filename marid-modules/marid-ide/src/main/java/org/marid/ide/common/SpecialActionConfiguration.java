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
package org.marid.ide.common;

import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class SpecialActionConfiguration {

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction editAction() {
        return new FxAction("edit", "ed", "Edit")
                .bindText("Edit...")
                .setAccelerator(KeyCombination.valueOf("F4"))
                .setIcon(M_EDIT)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction addAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Add")
                .setAccelerator(KeyCombination.valueOf("Ctrl+I"))
                .setIcon(M_ADD_BOX)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction removeAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Remove")
                .setAccelerator(KeyCombination.valueOf("Ctrl+D"))
                .setIcon(D_MINUS_BOX)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction cutAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Cut")
                .setAccelerator(KeyCombination.valueOf("Ctrl+X"))
                .setIcon(M_CONTENT_CUT)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction copyAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Copy")
                .setAccelerator(KeyCombination.valueOf("Ctrl+C"))
                .setIcon(M_CONTENT_COPY)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction pasteAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Paste")
                .setAccelerator(KeyCombination.valueOf("Ctrl+V"))
                .setIcon(M_CONTENT_PASTE)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction clearAllAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Clear All")
                .setIcon(M_CLEAR_ALL)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction renameAction() {
        return new FxAction("edit", "ed", "Edit")
                .bindText("Rename")
                .setAccelerator(KeyCombination.valueOf("Ctrl+R"))
                .setIcon(D_RENAME_BOX)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("specialAction")
    public FxAction selecatAllAction() {
        return new FxAction("sel", "sel", "Edit")
                .bindText("Select All")
                .setAccelerator(KeyCombination.valueOf("Ctrl+A"))
                .setIcon(D_SELECT_ALL)
                .setDisabled(true);
    }
}
