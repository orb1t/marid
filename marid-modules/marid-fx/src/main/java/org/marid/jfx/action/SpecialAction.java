/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.jfx.action;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCombination;
import org.marid.jfx.LocalizedStrings;

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
	private ObservableValue<Boolean> disabled;
	private ObservableValue<Boolean> selected;
	private ObservableValue<EventHandler<ActionEvent>> eventHandler;

	private boolean sealed;

	public SpecialAction(@Nonnull String toolbarGroup, @Nonnull String group, @Nonnull String menu) {
		super(toolbarGroup, group, menu);
	}

	@Override
	public SpecialAction bindAccelerator(ObservableValue<KeyCombination> value) {
		if (!sealed) accelerator = value;
		return (SpecialAction) super.bindAccelerator(value);
	}

	@Override
	public SpecialAction setAccelerator(KeyCombination value) {
		return bindAccelerator(new SimpleObjectProperty<>(value));
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
		return bindText(LocalizedStrings.ls(format, args));
	}

	@Override
	public SpecialAction bindDisabled(ObservableValue<Boolean> value) {
		if (!sealed) disabled = value;
		return (SpecialAction) super.bindDisabled(value);
	}

	@Override
	public SpecialAction setDisabled(boolean value) {
		return bindDisabled(new SimpleBooleanProperty(value));
	}

	@Override
	public SpecialAction bindSelected(ObservableValue<Boolean> value) {
		if (!sealed) selected = value;
		return (SpecialAction) super.bindSelected(value);
	}

	@Override
	public SpecialAction bindIcon(ObservableValue<String> value) {
		if (!sealed) icon = value;
		return (SpecialAction) super.bindIcon(value);
	}

	@Override
	public SpecialAction setIcon(String value) {
		return bindIcon(new SimpleStringProperty(value));
	}

	@Override
	public SpecialAction bindEventHandler(ObservableValue<EventHandler<ActionEvent>> value) {
		if (!sealed) eventHandler = value;
		return (SpecialAction) super.bindEventHandler(value);
	}

	@Override
	public SpecialAction setEventHandler(EventHandler<ActionEvent> value) {
		return bindEventHandler(new SimpleObjectProperty<>(value));
	}

	@PostConstruct
	public void seal() {
		sealed = true;
	}

	public void reset() {
		if (sealed) {
			super.accelerator.bind(accelerator == null ? new SimpleObjectProperty<>() : accelerator);
			super.description.bind(description == null ? new SimpleStringProperty() : description);
			super.text.bind(text == null ? new SimpleStringProperty() : text);
			super.disabled.bind(disabled == null ? new SimpleBooleanProperty() : disabled);
			super.selected.bind(selected == null ? new SimpleObjectProperty<>() : selected);
			super.icon.bind(icon == null ? new SimpleStringProperty() : icon);
			super.eventHandler.bind(eventHandler == null ? new SimpleObjectProperty<>() : eventHandler);

			children.clear();
		}
	}

	public void copy(FxAction action) {
		if (action.accelerator.isBound()) super.accelerator.bind(action.accelerator);
		if (action.description.isBound()) super.description.bind(action.description);
		if (action.text.isBound()) super.text.bind(action.text);
		if (action.icon.isBound()) super.icon.bind(action.icon);

		super.disabled.bind(action.disabled);
		super.selected.bind(action.selected);
		super.eventHandler.bind(action.eventHandler);

		children.addAll(action.children);
	}
}
