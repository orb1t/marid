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

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.beans.AbstractObservable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FxAction extends AbstractObservable {

    public final String toolbarGroup;
    public final String group;
    public final String menu;

    public final Map<String, FxAction> children = new TreeMap<>();

    protected ObservableValue<String> text;
    protected ObservableValue<KeyCombination> accelerator;
    protected ObservableValue<String> icon;
    protected ObservableValue<String> description;
    protected ObservableValue<Tooltip> hint;
    protected ObservableValue<Boolean> disabled;
    protected ObservableValue<Boolean> selected;
    protected ObservableValue<EventHandler<ActionEvent>> eventHandler;

    public SpecialAction specialAction;

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
        return text == null ? null : text.getValue();
    }

    public FxAction bindText(ObservableValue<String> value) {
        text = value;
        return this;
    }

    public FxAction bindText(String format, Object... args) {
        return bindText(LocalizedStrings.ls(format, args));
    }

    public FxAction setAccelerator(KeyCombination value) {
        return bindAccelerator(new SimpleObjectProperty<>(value));
    }

    public FxAction bindAccelerator(ObservableValue<KeyCombination> value) {
        accelerator = value;
        return this;
    }

    public String getIcon() {
        return icon == null ? null : icon.getValue();
    }

    public FxAction setIcon(@PropertyKey(resourceBundle = "fonts.meta") String value) {
        return bindIcon(new SimpleStringProperty(value));
    }

    public FxAction bindIcon(ObservableValue<String> value) {
        icon = value;
        return this;
    }

    public FxAction setDisabled(boolean value) {
        return bindDisabled(new SimpleBooleanProperty(value));
    }

    public FxAction bindDisabled(ObservableValue<Boolean> value) {
        disabled = value;
        return this;
    }

    public FxAction bindDescription(ObservableValue<String> value) {
        description = value;
        return this;
    }

    public FxAction bindHint(ObservableValue<Tooltip> value) {
        hint = value;
        return this;
    }

    public FxAction bindEventHandler(ObservableValue<EventHandler<ActionEvent>> value) {
        eventHandler = value;
        return this;
    }

    public FxAction setEventHandler(EventHandler<ActionEvent> eventHandler) {
        return bindEventHandler(new SimpleObjectProperty<>(eventHandler));
    }

    public FxAction setSpecialAction(SpecialAction value) {
        specialAction = value;
        return this;
    }

    public FxAction bindSelected(ObservableValue<Boolean> value) {
        selected = value;
        return this;
    }

    public Binding<Text> icon(int size) {
        return Bindings.createObjectBinding(() -> {
            final String icon = getIcon();
            return icon != null ? glyphIcon(icon, size) : null;
        }, icon);
    }

    public MenuItem menuItem() {
        final MenuItem item;
        if (selected != null) {
            final CheckMenuItem checkMenuItem = new CheckMenuItem();
            item = checkMenuItem;
            if (eventHandler != null) item.onActionProperty().bind(eventHandler);
            if (selected != null) {
                final ChangeListener<Boolean> selectedListener = (o, oV, nV) -> checkMenuItem.setSelected(nV);
                checkMenuItem.setUserData(selectedListener);
                selected.addListener(new WeakChangeListener<>(selectedListener));
            }
            if (disabled != null) item.disableProperty().bind(disabled);
        } else if (!children.isEmpty()) {
            final Menu menu = new Menu();
            item = menu;
            final Menu[] menus = MaridActions.menus(children);
            for (int i = 0; i < menus.length; i++) {
                if (i > 0) {
                    menu.getItems().add(new SeparatorMenuItem());
                }
                final MenuItem[] items = menus[i].getItems().toArray(new MenuItem[0]);
                menus[i].getItems().clear();
                menu.getItems().addAll(items);
            }
        } else {
            item = new MenuItem();
            if (eventHandler != null) item.onActionProperty().bind(eventHandler);
            if (disabled != null) item.disableProperty().bind(disabled);
        }
        if (accelerator != null) item.acceleratorProperty().bind(accelerator);
        if (text != null) item.textProperty().bind(text);
        if (icon != null) item.graphicProperty().bind(icon(16));
        final AtomicReference<WeakInvalidationListener> listener = new AtomicReference<>();
        final InvalidationListener updater = o -> {
            removeListener(listener.get());
            final ObservableList<MenuItem> items = item.getParentMenu() != null
                    ? item.getParentMenu().getItems()
                    : item.getParentPopup() != null
                    ? item.getParentPopup().getItems()
                    : FXCollections.emptyObservableList();
            final int index = items.indexOf(item);
            if (index >= 0) {
                items.set(index, menuItem());
            }
        };
        item.setUserData(updater);
        listener.set(new WeakInvalidationListener(updater));
        addListener(listener.get());
        return item;
    }

    public Button button() {
        final ArrayList<Runnable> updaters = new ArrayList<>();
        final Button button = new Button();
        button.setFocusTraversable(false);
        updaters.add(() -> {
            if (children.isEmpty()) {
                button.disableProperty().unbind(); button.disableProperty().set(false);
                button.onActionProperty().unbind(); button.onActionProperty().set(null);
                if (disabled != null) button.disableProperty().bind(disabled);
                if (eventHandler != null) button.onActionProperty().bind(eventHandler);
            } else {
                button.disableProperty().bind(Bindings.createBooleanBinding(() -> false));
                button.onActionProperty().bind(Bindings.createObjectBinding(() -> event -> {
                    final ContextMenu contextMenu = new ContextMenu(MaridActions.contextMenu(children));
                    contextMenu.show(button, Side.BOTTOM, 0, 0);
                }));
            }
            button.graphicProperty().unbind(); button.graphicProperty().set(null);
            button.tooltipProperty().unbind(); button.tooltipProperty().set(null);
            if (icon != null) button.graphicProperty().bind(icon(20));
            if (hint != null) button.tooltipProperty().bind(hint);
        });
        final InvalidationListener updater = o -> updaters.forEach(Runnable::run);
        button.setUserData(updater);
        addListener(new WeakInvalidationListener(updater));
        updater.invalidated(this);
        return button;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", group, toolbarGroup, menu);
    }
}
