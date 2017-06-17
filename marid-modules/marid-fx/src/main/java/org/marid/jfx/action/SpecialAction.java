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
    private EventHandler<ActionEvent> eventHandler;

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

    private void unbindAccelerator() {
        ((FxAction) this).accelerator.unbind();
        ((FxAction) this).accelerator.set(null);
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

    private void unbindDescription() {
        ((FxAction) this).description.unbind();
        ((FxAction) this).description.set(null);
    }

    @Override
    public SpecialAction setDescription(String value) {
        return (SpecialAction) super.setDescription(value);
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

    private void unbindText() {
        ((FxAction) this).text.unbind();
        ((FxAction) this).text.set(null);
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

    private void unbindDisabled() {
        ((FxAction) this).disabled.unbind();
        ((FxAction) this).disabled.set(false);
    }

    @Override
    public SpecialAction bindSelected(ObservableValue<Boolean> value) {
        if (!sealed) selected = value;
        return (SpecialAction) super.bindSelected(value);
    }

    @Override
    public SpecialAction setSelected(Boolean value) {
        return (SpecialAction) super.setSelected(value);
    }

    private void unbindSelected() {
        ((FxAction) this).selected.unbind();
        ((FxAction) this).selected.set(null);
    }

    @Override
    public SpecialAction bindHint(ObservableValue<Tooltip> value) {
        if (!sealed) hint = value;
        return (SpecialAction) super.bindHint(value);
    }

    private void unbindHint() {
        ((FxAction) this).hint.unbind();
        ((FxAction) this).hint.set(null);
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

    private void unbindIcon() {
        ((FxAction) this).icon.unbind();
        ((FxAction) this).icon.set(null);
    }

    @Override
    public SpecialAction setEventHandler(EventHandler<ActionEvent> value) {
        if (!sealed) eventHandler = value;
        return (SpecialAction) super.setEventHandler(value);
    }

    public void seal() {
        sealed = true;
    }

    public void reset() {
        if (sealed) {
            if (accelerator != null) bindAccelerator(accelerator); else unbindAccelerator();
            if (description != null) bindDescription(description); else unbindDescription();
            if (text != null) bindText(text); else unbindText();
            if (disabled != null) bindDisabled(disabled); else unbindDisabled();
            if (selected != null) bindSelected(selected); else unbindSelected();
            if (hint != null) bindHint(hint); else unbindHint();
            if (icon != null) bindIcon(icon); else unbindIcon();
            setEventHandler(null);
        }
    }

    public void copy(FxAction action) {
        bindAccelerator(action.accelerator);
        bindDescription(action.description);
        bindText(action.text);
        bindDisabled(action.disabled);
        bindSelected(action.selected);
        bindHint(action.hint);
        bindIcon(action.icon);
        setEventHandler(action.getEventHandler());
    }
}
