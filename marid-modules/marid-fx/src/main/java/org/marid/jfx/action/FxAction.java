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
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FxAction {

    public final String toolbarGroup;
    public final String group;
    public final String menu;

    protected final ObservableList<FxAction> children = observableArrayList();
    public final List<Object> anchors = new LinkedList<>();

    protected final StringProperty text = new SimpleStringProperty(this, "text");
    protected final ObjectProperty<KeyCombination> accelerator = new SimpleObjectProperty<>(this, "accelerator");
    protected final StringProperty icon = new SimpleStringProperty(this, "icon");
    protected final StringProperty description = new SimpleStringProperty(this, "description");
    protected final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled");
    protected final ObjectProperty<Boolean> selected = new SimpleObjectProperty<>(this, "selected");
    protected final ObjectProperty<EventHandler<ActionEvent>> eventHandler = new SimpleObjectProperty<>(this, "eventHandler");

    public SpecialAction specialAction;

    public FxAction(String toolbarGroup, String group, String menu) {
        this.toolbarGroup = toolbarGroup;
        this.group = group;
        this.menu = menu;
    }

    public FxAction(String toolbarGroup) {
        this(toolbarGroup, null, null);
    }

    public FxAction(String group, String menu) {
        this(null, group, menu);
    }

    public String getText() {
        return text.getValue();
    }

    public FxAction bindText(ObservableValue<String> value) {
        text.bind(value);
        return this;
    }

    public FxAction bindText(String format, Object... args) {
        return bindText(LocalizedStrings.ls(format, args));
    }

    public FxAction setAccelerator(KeyCombination value) {
        return bindAccelerator(new SimpleObjectProperty<>(value));
    }

    public FxAction bindAccelerator(ObservableValue<KeyCombination> value) {
        accelerator.bind(value);
        return this;
    }

    public String getIcon() {
        return icon.getValue();
    }

    public FxAction setIcon(@PropertyKey(resourceBundle = "fonts.meta") String value) {
        return bindIcon(new SimpleStringProperty(value));
    }

    public FxAction bindIcon(ObservableValue<String> value) {
        icon.bind(value);
        return this;
    }

    public FxAction setDisabled(boolean value) {
        return bindDisabled(new SimpleBooleanProperty(value));
    }

    public FxAction bindDisabled(ObservableValue<Boolean> value) {
        disabled.bind(value);
        return this;
    }

    public FxAction bindDescription(ObservableValue<String> value) {
        description.bind(value);
        return this;
    }

    public FxAction bindEventHandler(ObservableValue<EventHandler<ActionEvent>> value) {
        eventHandler.bind(value);
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
        selected.bind(value);
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
        if (selected.getValue() != null) {
            final CheckMenuItem checkMenuItem = new CheckMenuItem();
            item = checkMenuItem;
            item.onActionProperty().bind(eventHandler);
            checkMenuItem.selectedProperty().bindBidirectional(selected);
            item.disableProperty().bind(disabled);
        } else if (!children.isEmpty()) {
            final Menu menu = new Menu();
            item = menu;
            menu.getItems().setAll(grouped(menu.getItems(), children));
        } else {
            item = new MenuItem();
            item.onActionProperty().bind(eventHandler);
            item.disableProperty().bind(disabled);
        }
        item.acceleratorProperty().bind(accelerator);
        item.textProperty().bind(text);
        item.graphicProperty().bind(icon(16));

        final AtomicReference<WeakInvalidationListener> listener = new AtomicReference<>();
        final InvalidationListener updater = o -> {
            selected.removeListener(listener.get());
            children.removeListener(listener.get());
            final int index = list.indexOf(item);
            if (index >= 0) {
                list.set(index, menuItem(list));
            }
        };
        item.setUserData(updater);
        listener.set(new WeakInvalidationListener(updater));
        selected.addListener(listener.get());
        children.addListener(listener.get());
        return item;
    }

    public Button button() {
        final Button button = new Button();
        button.setFocusTraversable(false);
        button.graphicProperty().bind(icon(20));
        button.tooltipProperty().bind(Bindings.createObjectBinding(() -> {
            final String hint = getText();
            if (hint == null || hint.isEmpty()) {
                return null;
            } else {
                final Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bind(text);
                return tooltip;
            }
        }, text));
        button.setOnAction(event -> {
            if (children.isEmpty()) {
                final EventHandler<ActionEvent> h = eventHandler.get();
                if (h != null) {
                    h.handle(event);
                }
            } else {
                final ContextMenu contextMenu = grouped(children);
                contextMenu.show(button, Side.BOTTOM, 0, 0);
            }
        });
        button.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            if (!children.isEmpty()) {
                return false;
            } else {
                return disabled.get();
            }
        }, children, disabled));
        return button;
    }

    public ObservableList<FxAction> getChildren() {
        return FXCollections.unmodifiableObservableList(children);
    }

    public FxAction setChildren(FxAction... actions) {
        children.setAll(actions);
        return this;
    }

    public FxAction setChildren(Collection<? extends FxAction> actions) {
        children.setAll(actions);
        return this;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public EventHandler<ActionEvent> getEventHandler() {
        return eventHandler.get();
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", group, toolbarGroup, menu);
    }
}
