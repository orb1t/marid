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

package org.marid.jfx.action;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialAction extends FxAction {

    private ObservableValue<String> text;
    private ObservableValue<KeyCombination> accelerator;
    private ObservableValue<String> icon;
    private ObservableValue<String> description;
    private ObservableValue<Tooltip> hint;
    private ObservableValue<Boolean> disabled;
    private ObservableValue<Boolean> selected;
    private ObservableValue<EventHandler<ActionEvent>> eventHandler;

    private boolean sealed;

    public SpecialAction(@Nonnull String toolbarGroup, @Nonnull String group, @Nonnull String menu) {
        super(toolbarGroup, group, menu);
    }

    public SpecialAction(@Nonnull String toolbarGroup) {
        super(toolbarGroup);
    }

    public SpecialAction(@Nonnull String group, @Nonnull String menu) {
        super(group, menu);
    }

    @Override
    public SpecialAction bindAccelerator(ObservableValue<KeyCombination> value) {
        if (!sealed) accelerator = value;
        return (SpecialAction) super.bindAccelerator(value);
    }

    @Override
    public SpecialAction setAccelerator(KeyCombination value) {
        return (SpecialAction) super.setAccelerator(value);
    }

    @Override
    public SpecialAction bindDescription(ObservableValue<String> value) {
        if (!sealed) description = value;
        return (SpecialAction) super.bindDescription(value);
    }

    @Override
    public SpecialAction bindText(ObservableValue<String> value) {
        if (!sealed) text = value;
        return (SpecialAction) super.bindText(value);
    }

    @Override
    public SpecialAction bindText(String format, Object... args) {
        return (SpecialAction) super.bindText(format, args);
    }

    @Override
    public SpecialAction bindDisabled(ObservableValue<Boolean> value) {
        if (!sealed) disabled = value;
        return (SpecialAction) super.bindDisabled(value);
    }

    @Override
    public SpecialAction setDisabled(boolean value) {
        return (SpecialAction) super.setDisabled(value);
    }

    @Override
    public SpecialAction bindSelected(ObservableValue<Boolean> value) {
        if (!sealed) selected = value;
        return (SpecialAction) super.bindSelected(value);
    }

    @Override
    public SpecialAction bindHint(ObservableValue<Tooltip> value) {
        if (!sealed) hint = value;
        return (SpecialAction) super.bindHint(value);
    }

    @Override
    public SpecialAction bindIcon(ObservableValue<String> value) {
        if (!sealed) icon = value;
        return (SpecialAction) super.bindIcon(value);
    }

    @Override
    public SpecialAction setIcon(String value) {
        return (SpecialAction) super.setIcon(value);
    }

    @Override
    public SpecialAction bindEventHandler(ObservableValue<EventHandler<ActionEvent>> value) {
        if (!sealed) eventHandler = value;
        return (SpecialAction) super.bindEventHandler(value);
    }

    @Override
    public SpecialAction setEventHandler(EventHandler<ActionEvent> value) {
        return (SpecialAction) super.setEventHandler(value);
    }

    @PostConstruct
    public void seal() {
        sealed = true;
    }

    public void reset() {
        if (sealed) {
            super.accelerator = accelerator;
            super.description = description;
            super.text = text;
            super.disabled = disabled;
            super.selected = selected;
            super.hint = hint;
            super.icon = icon;
            super.eventHandler = eventHandler;

            children.clear();
        }
    }

    public void copy(FxAction action) {
        if (action.accelerator != null) super.accelerator = action.accelerator;
        if (action.description != null) super.description = action.description;
        if (action.text != null) super.text = action.text;
        if (action.disabled != null) super.disabled = action.disabled;
        if (action.selected != null) super.selected = action.selected;
        if (action.hint != null) super.hint = action.hint;
        if (action.icon != null) super.icon = action.icon;
        if (action.eventHandler != null) super.eventHandler = action.eventHandler;

        children.putAll(action.children);
    }

    public void update() {
        fireInvalidate(this);
    }
}
