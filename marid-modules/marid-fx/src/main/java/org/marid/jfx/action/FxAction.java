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
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.LocalizedStrings;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FxAction implements Observable {

    protected final Collection<InvalidationListener> listeners = new ConcurrentLinkedQueue<>();

    public final String toolbarGroup;
    public final String group;
    public final String menu;

    protected final ObservableList<FxAction> children = observableArrayList();

    protected ObservableValue<String> text;
    protected ObservableValue<KeyCombination> accelerator;
    protected ObservableValue<String> icon;
    protected ObservableValue<String> description;
    protected ObservableValue<Tooltip> hint;
    protected ObservableValue<Boolean> disabled;
    protected ObservableValue<Boolean> selected;
    protected ObservableValue<EventHandler<ActionEvent>> eventHandler;

    public SpecialAction specialAction;

    public FxAction(String toolbarGroup, String group, String menu) {
        this.toolbarGroup = toolbarGroup;
        this.group = group;
        this.menu = menu;

        children.addListener((InvalidationListener) observable -> listeners.forEach(l -> l.invalidated(observable)));
    }

    public FxAction(String toolbarGroup) {
        this(toolbarGroup, null, null);
    }

    public FxAction(String group, String menu) {
        this(null, group, menu);
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

    public static MenuItem[] grouped(List<MenuItem> list, Collection<FxAction> actions) {
        final Map<String, List<FxAction>> map = actions.stream()
                .collect(Collectors.groupingBy(a -> a.group, TreeMap::new, Collectors.toList()));
        return map.values().stream()
                .reduce(new ArrayList<MenuItem>(), (a, e) -> {
                    if (!a.isEmpty()) {
                        a.add(new SeparatorMenuItem());
                    }
                    a.addAll(e.stream().map(ac -> ac.menuItem(list)).collect(Collectors.toList()));
                    return a;
                }, (a1, a2) -> a2).toArray(new MenuItem[0]);
    }

    public static ContextMenu grouped(Collection<FxAction> actions) {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(grouped(contextMenu.getItems(), actions));
        return contextMenu;
    }

    public MenuItem[] grouped(List<MenuItem> list) {
        return grouped(list, children);
    }

    public MenuItem menuItem(List<MenuItem> list) {
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
            menu.getItems().setAll(grouped(list, children));
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
            final int index = list.indexOf(item);
            if (index >= 0) {
                list.set(index, menuItem(list));
            }
        };
        item.setUserData(updater);
        listener.set(new WeakInvalidationListener(updater));
        addListener(listener.get());
        return item;
    }

    public Button button() {
        final Button button = new Button();
        button.setFocusTraversable(false);
        final InvalidationListener updater = o -> {
            if (children.isEmpty()) {
                button.disableProperty().unbind(); button.disableProperty().set(false);
                button.onActionProperty().unbind(); button.onActionProperty().set(null);
                if (disabled != null) button.disableProperty().bind(disabled);
                if (eventHandler != null) button.onActionProperty().bind(eventHandler);
            } else {
                button.disableProperty().unbind(); button.disableProperty().set(false);
                button.onActionProperty().unbind(); button.setOnAction(event -> {
                    final ContextMenu contextMenu = grouped(children);
                    contextMenu.show(button, Side.BOTTOM, 0, 0);
                });
            }
            button.graphicProperty().unbind(); button.graphicProperty().set(null);
            button.tooltipProperty().unbind(); button.tooltipProperty().set(null);
            if (icon != null) button.graphicProperty().bind(icon(20));
            if (hint != null) {
                button.tooltipProperty().bind(hint);
            } else if (text != null) {
                button.tooltipProperty().bind(Bindings.createObjectBinding(() -> {
                    final Tooltip tooltip = new Tooltip();
                    tooltip.setText(text.getValue());
                    return tooltip;
                }, text));
            }
        };
        button.setUserData(updater);
        addListener(new WeakInvalidationListener(updater));
        updater.invalidated(this);
        return button;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

    public List<FxAction> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void setChildren(FxAction... actions) {
        children.setAll(actions);
    }

    public void setChildren(Collection<? extends FxAction> actions) {
        children.setAll(actions);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", group, toolbarGroup, menu);
    }
}
