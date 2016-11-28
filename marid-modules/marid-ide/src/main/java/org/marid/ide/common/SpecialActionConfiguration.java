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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class SpecialActionConfiguration {

    public static final String EDIT = "editAction";
    public static final String ADD = "addAction";
    public static final String REMOVE = "removeAction";
    public static final String CUT = "cutAction";
    public static final String COPY = "copyAction";
    public static final String PASTE = "pasteAction";

    @Bean(name = EDIT)
    @IdeAction
    public FxAction editAction() {
        return new FxAction("edit", "ed", "Edit")
                .bindText("Edit...")
                .setAccelerator(KeyCombination.valueOf("F4"))
                .setIcon(M_FOLDER_SHARED)
                .setDisabled(true);
    }

    @Bean(name = ADD)
    @IdeAction
    public FxAction addAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Add")
                .setAccelerator(KeyCombination.valueOf("Ctrl+Plus"))
                .setIcon(M_ADD_BOX)
                .setDisabled(true);
    }

    @Bean(name = REMOVE)
    @IdeAction
    public FxAction removeAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Remove")
                .setAccelerator(KeyCombination.valueOf("Ctrl+Minus"))
                .setIcon(D_MINUS_BOX)
                .setDisabled(true);
    }

    @Bean(name = CUT)
    @IdeAction
    public FxAction cutAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Cut")
                .setAccelerator(KeyCombination.valueOf("Ctrl+X"))
                .setIcon(M_CONTENT_CUT)
                .setDisabled(true);
    }

    @Bean(name = COPY)
    @IdeAction
    public FxAction copyAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Copy")
                .setAccelerator(KeyCombination.valueOf("Ctrl+C"))
                .setIcon(M_CONTENT_COPY)
                .setDisabled(true);
    }

    @Bean(name = PASTE)
    @IdeAction
    public FxAction pasteAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Paste")
                .setAccelerator(KeyCombination.valueOf("Ctrl+V"))
                .setIcon(M_CONTENT_PASTE)
                .setDisabled(true);
    }
}
