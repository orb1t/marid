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

package org.marid.jfx.toolbar;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.util.Builder;
import org.marid.jfx.icons.FontIcons;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
public final class ToolbarBuilder implements Builder<ToolBar> {

    private final List<Node> nodes = new ArrayList<>();

    public ToolbarBuilder add(String tooltip, String icon, EventHandler<ActionEvent> eventHandler, Consumer<Button> buttonConsumer) {
        final Button button = new Button(null, FontIcons.glyphIcon(icon, 20));
        button.setFocusTraversable(false);
        if (tooltip != null) {
            final Tooltip t = new Tooltip();
            t.textProperty().bind(ls(tooltip));
            button.setTooltip(t);
        }
        button.setOnAction(eventHandler);
        buttonConsumer.accept(button);
        nodes.add(button);
        return this;
    }

    public ToolbarBuilder add(String tooltip, String icon, EventHandler<ActionEvent> eventHandler, BooleanBinding disabled) {
        return add(tooltip, icon, eventHandler, button -> button.disableProperty().bind(disabled));
    }

    public ToolbarBuilder add(String tooltip, String icon, EventHandler<ActionEvent> eventHandler) {
        return add(tooltip, icon, eventHandler, button -> {});
    }

    public <T extends Node> ToolbarBuilder add(T node, Consumer<T> nodeConsumer) {
        nodeConsumer.accept(node);
        node.setFocusTraversable(false);
        nodes.add(node);
        return this;
    }

    public ToolbarBuilder addSeparator() {
        nodes.add(new Separator());
        return this;
    }

    @Override
    public ToolBar build() {
        return new ToolBar(nodes.toArray(new Node[nodes.size()]));
    }

    public ToolBar build(Consumer<ToolBar> toolBarConsumer) {
        final ToolBar toolBar = build();
        toolBarConsumer.accept(toolBar);
        return toolBar;
    }
}
