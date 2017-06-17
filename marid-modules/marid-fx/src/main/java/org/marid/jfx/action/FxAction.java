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

package org.marid.jfx.action;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.beans.ConstantValue;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class FxAction {

    public final String toolbarGroup;
    public final String group;
    public final String menu;
    public final Map<String, FxAction> children = new TreeMap<>();
    public final StringProperty text = new SimpleStringProperty();
    public final ObjectProperty<KeyCombination> accelerator = new SimpleObjectProperty<>();
    public final StringProperty icon = new SimpleStringProperty();
    public final StringProperty description = new SimpleStringProperty();
    public final ObjectProperty<Tooltip> hint = new SimpleObjectProperty<>();
    public final BooleanProperty disabled = new SimpleBooleanProperty();
    public final ObjectProperty<Boolean> selected = new SimpleObjectProperty<>();

    private EventHandler<ActionEvent> eventHandler;

    public FxAction(@Nonnull String toolbarGroup, @Nonnull String group, @Nonnull String menu) {
        this.toolbarGroup = toolbarGroup;
        this.group = group;
        this.menu = menu;
    }

    public FxAction(@Nonnull String toolbarGroup) {
        this.toolbarGroup = toolbarGroup;
        this.group = null;
        this.menu = null;
    }

    public FxAction(@Nonnull String group, @Nonnull String menu) {
        this.toolbarGroup = null;
        this.group = group;
        this.menu = menu;
    }

    public String getToolbarGroup() {
        return toolbarGroup;
    }

    public String getGroup() {
        return group;
    }

    public String getMenu() {
        return menu;
    }

    public String getText() {
        return text.get();
    }

    public FxAction bindText(ObservableValue<String> value) {
        text.bind(value);
        return this;
    }

    public FxAction bindText(String format, Object... args) {
        return bindText(LocalizedStrings.ls(format, args));
    }

    public FxAction setAccelerator(KeyCombination value) {
        return bindAccelerator(ConstantValue.value(value));
    }

    public FxAction bindAccelerator(ObservableValue<KeyCombination> value) {
        accelerator.bind(value);
        return this;
    }

    public String getIcon() {
        return icon.get();
    }

    public FxAction setIcon(@PropertyKey(resourceBundle = "fonts.meta") String value) {
        return bindIcon(ConstantValue.value(value));
    }

    public FxAction bindIcon(ObservableValue<String> value) {
        icon.bind(value);
        return this;
    }

    public boolean getDisabled() {
        return disabled.get();
    }

    public FxAction setDisabled(boolean value) {
        return bindDisabled(ConstantValue.value(value));
    }

    public FxAction bindDisabled(ObservableValue<Boolean> value) {
        disabled.bind(value);
        return this;
    }

    public String getDescription() {
        return description.get();
    }

    public FxAction setDescription(String value) {
        return bindDescription(ConstantValue.value(value));
    }

    public FxAction bindDescription(ObservableValue<String> value) {
        description.bind(value);
        return this;
    }

    public Tooltip getHint() {
        return hint.get();
    }

    public FxAction bindHint(ObservableValue<Tooltip> value) {
        hint.bind(value);
        return this;
    }

    public EventHandler<ActionEvent> getEventHandler() {
        return eventHandler;
    }

    public FxAction setEventHandler(EventHandler<ActionEvent> eventHandler) {
        this.eventHandler = eventHandler;
        return this;
    }

    public FxAction setSelected(Boolean value) {
        return bindSelected(ConstantValue.value(value));
    }

    public Boolean getSelected() {
        return selected.get();
    }

    public FxAction bindSelected(ObservableValue<Boolean> value) {
        selected.bind(value);
        return this;
    }

    public FxAction setChildren(Map<String, FxAction> actions) {
        children.clear();
        children.putAll(actions);
        return this;
    }

    public FxAction addChild(String name, FxAction action) {
        children.put(name, action);
        return this;
    }

    public FxAction addChildren(Map<String, FxAction> actions) {
        children.putAll(actions);
        return this;
    }
}
