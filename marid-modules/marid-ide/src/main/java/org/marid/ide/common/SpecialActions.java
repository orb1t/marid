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

import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class SpecialActions {

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction editAction() {
        return new FxAction("edit", "ed", "Edit")
                .bindText("Edit...")
                .setAccelerator(KeyCombination.valueOf("F4"))
                .setIcon(M_FOLDER_SHARED)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction addAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Add")
                .setAccelerator(KeyCombination.valueOf("Ctrl+Plus"))
                .setIcon(M_ADD)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction removeAction() {
        return new FxAction("mod", "mod", "Edit")
                .bindText("Remove")
                .setAccelerator(KeyCombination.valueOf("Ctrl+Minus"))
                .setIcon(M_REMOVE)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction cutAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Cut")
                .setAccelerator(KeyCombination.valueOf("Ctrl+X"))
                .setIcon(M_CONTENT_CUT)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction copyAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Copy")
                .setAccelerator(KeyCombination.valueOf("Ctrl+C"))
                .setIcon(M_CONTENT_COPY)
                .setDisabled(true);
    }

    @Bean
    @IdeAction
    @Qualifier("special")
    public FxAction pasteAction() {
        return new FxAction("cp", "cp", "Edit")
                .bindText("Paste")
                .setAccelerator(KeyCombination.valueOf("Ctrl+V"))
                .setIcon(M_CONTENT_PASTE)
                .setDisabled(true);
    }

    public void setEditAction(Node node, ObservableStringValue text, EventHandler<ActionEvent> eventHandler) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                editAction().setDisabled(false);
                editAction().setEventHandler(eventHandler);
                editAction().bindText(text);
            } else {
                editAction().setEventHandler(event -> {});
                editAction().setDisabled(true);
                editAction().bindText(ls("Edit..."));
            }
        });
    }
}
