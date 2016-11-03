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
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCombination;

/**
 * @author Dmitry Ovchinnikov
 */
public final class FxAction {

    private final String toolbarGroup;
    private final String group;
    private final String menu;
    private StringProperty text;
    private ObjectProperty<KeyCombination> accelerator;
    private StringProperty icon;
    private StringProperty description;
    private StringProperty hint;
    private BooleanProperty disabled;
    private BooleanProperty selected;
    private EventHandler<ActionEvent> eventHandler;

    public FxAction(String toolbarGroup, String group, String menu) {
        this.toolbarGroup = toolbarGroup;
        this.group = group;
        this.menu = menu;
    }

    public FxAction(String toolbarGroup) {
        this(toolbarGroup, null, null);
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
        return text == null ? null : text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public FxAction bindText(ObservableStringValue value) {
        if (text == null) {
            text = new SimpleStringProperty();
        }
        text.bind(value);
        return this;
    }

    public KeyCombination getAccelerator() {
        return accelerator == null ? null : accelerator.get();
    }

    public FxAction setAccelerator(KeyCombination accelerator) {
        if (this.accelerator == null) {
            this.accelerator = new SimpleObjectProperty<>();
        }
        this.accelerator.set(accelerator);
        return this;
    }

    public ObjectProperty<KeyCombination> acceleratorProperty() {
        return accelerator;
    }

    public FxAction bindAccelerator(ObservableValue<KeyCombination> value) {
        if (accelerator == null) {
            accelerator = new SimpleObjectProperty<>();
        }
        accelerator.bind(value);
        return this;
    }

    public String getIcon() {
        return icon == null ? null : icon.get();
    }

    public FxAction setIcon(String icon) {
        if (this.icon == null) {
            this.icon = new SimpleStringProperty();
        }
        this.icon.set(icon);
        return this;
    }

    public StringProperty iconProperty() {
        return icon;
    }

    public FxAction bindIcon(ObservableStringValue value) {
        if (icon == null) {
            icon = new SimpleStringProperty();
        }
        icon.bind(value);
        return this;
    }

    public boolean getDisabled() {
        return disabled == null ? null : disabled.get();
    }

    public FxAction setDisabled(boolean disabled) {
        if (this.disabled == null) {
            this.disabled = new SimpleBooleanProperty();
        }
        this.disabled.set(disabled);
        return this;
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public FxAction bindDisabled(ObservableBooleanValue value) {
        if (disabled == null) {
            disabled = new SimpleBooleanProperty();
        }
        disabled.bind(value);
        return this;
    }

    public String getDescription() {
        return description == null ? null : description.get();
    }

    public FxAction setDescription(String description) {
        if (this.description == null) {
            this.description = new SimpleStringProperty();
        }
        this.description.set(description);
        return this;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public FxAction bindDescription(ObservableStringValue value) {
        if (description == null) {
            description = new SimpleStringProperty();
        }
        description.bind(value);
        return this;
    }

    public String getHint() {
        return hint == null ? null : hint.get();
    }

    public FxAction setHint(String hint) {
        if (this.hint == null) {
            this.hint = new SimpleStringProperty();
        }
        this.hint.set(hint);
        return this;
    }

    public StringProperty hintProperty() {
        return hint;
    }

    public FxAction bindHint(ObservableStringValue value) {
        if (hint == null) {
            hint = new SimpleStringProperty();
        }
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

    public FxAction setSelected(boolean selected) {
        if (this.selected == null) {
            this.selected = new SimpleBooleanProperty();
        }
        this.selected.set(selected);
        return this;
    }

    public boolean getSelected() {
        return selected == null ? null : selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public FxAction bindSelected(ObservableBooleanValue value) {
        if (selected == null) {
            selected = new SimpleBooleanProperty();
        }
        selected.bind(value);
        return this;
    }
}
