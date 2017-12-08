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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FxAction {

  private final InvalidationListener listener = o -> invalidate();
  private final WeakInvalidationListener wlistener = new WeakInvalidationListener(listener);
  private final WeakHashMap<MenuItem, ObservableList<MenuItem>> menus = new WeakHashMap<>();
  private final WeakHashMap<ButtonBase, ObservableList<Node>> buttons = new WeakHashMap<>();

  public final String toolbarGroup;
  public final String group;
  public final String menu;
  public final SpecialAction specialAction;

  ObservableValue<String> text;
  ObservableValue<KeyCombination> accelerator;
  ObservableValue<String> icon;
  ObservableValue<String> description;
  ObservableValue<Boolean> disabled;
  ObservableValue<Boolean> selected;
  ObservableValue<EventHandler<ActionEvent>> eventHandler;
  ObservableValue<ObservableList<FxAction>> children;

  public FxAction(String toolbarGroup, String group, String menu, SpecialAction specialAction) {
    this.toolbarGroup = toolbarGroup;
    this.group = group;
    this.menu = menu;
    this.specialAction = specialAction;

    if (specialAction != null) {
      text = ls(specialAction.text);
      accelerator = new SimpleObjectProperty<>(specialAction.accelerator);
      icon = new SimpleStringProperty(specialAction.icon);
      description = ls(specialAction.description);
    }

    disabled = new SimpleBooleanProperty(false);
  }

  public FxAction(String group, SpecialAction specialAction) {
    this(specialAction.toolbarGroup, group, specialAction.menu, specialAction);
  }

  public FxAction(String toolbarGroup, String group, String menu) {
    this(toolbarGroup, group, menu, null);
  }

  public FxAction(String toolbarGroup) {
    this(toolbarGroup, null, null);
  }

  public FxAction(String group, String menu) {
    this(null, group, menu);
  }

  public FxAction(SpecialAction specialAction) {
    this(specialAction.toolbarGroup, specialAction.group, specialAction.menu, specialAction);
  }

  void invalidate() {
    menus.forEach((m, ms) -> {
      final int index = ms.indexOf(m);
      if (index >= 0) {
        Platform.runLater(() -> ms.set(index, menuItem(ms)));
      }
    });
    buttons.forEach((b, bs) -> {
      final int index = bs.indexOf(b);
      if (index >= 0) {
        Platform.runLater(() -> bs.set(index, button(bs)));
      }
    });
  }

  private <T> ObservableValue<T> set(ObservableValue<T> newValue, ObservableValue<T> oldValue) {
    if (oldValue != null) {
      oldValue.removeListener(wlistener);
    }
    if (newValue != null) {
      newValue.addListener(wlistener);
    }
    return newValue;
  }

  public FxAction bindText(ObservableValue<String> value) {
    text = value;
    return this;
  }

  public FxAction bindText(String format, Object... args) {
    return bindText(ls(format, args));
  }

  public FxAction setAccelerator(KeyCombination value) {
    return bindAccelerator(new SimpleObjectProperty<>(value));
  }

  public FxAction bindAccelerator(ObservableValue<KeyCombination> value) {
    accelerator = value;
    return this;
  }

  public String getIcon() {
    return icon.getValue();
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

  public FxAction bindEventHandler(ObservableValue<EventHandler<ActionEvent>> value) {
    eventHandler = value;
    return this;
  }

  public FxAction setEventHandler(EventHandler<ActionEvent> eventHandler) {
    return bindEventHandler(new SimpleObjectProperty<>(eventHandler));
  }

  public FxAction bindSelected(ObservableValue<Boolean> value) {
    selected = set(value, selected);
    return this;
  }

  public Binding<Text> icon(int size) {
    return createObjectBinding(() -> ofNullable(getIcon()).map(i -> glyphIcon(i, size)).orElse(null), icon);
  }

  public static MenuItem[] grouped(@Nonnull ObservableList<MenuItem> list, @Nonnull Collection<FxAction> actions) {
    final Map<String, List<FxAction>> map = actions.stream()
        .collect(Collectors.groupingBy(a -> a.group, TreeMap::new, toList()));
    return map.values().stream()
        .reduce(new ArrayList<MenuItem>(), (a, e) -> {
          if (!a.isEmpty()) {
            a.add(new SeparatorMenuItem());
          }
          a.addAll(e.stream().map(ac -> ac.menuItem(list)).collect(toList()));
          return a;
        }, (a1, a2) -> a2).toArray(new MenuItem[0]);
  }

  public static ContextMenu grouped(Collection<FxAction> actions) {
    final ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().setAll(grouped(contextMenu.getItems(), actions));
    return contextMenu;
  }

  public MenuItem menuItem(@Nonnull ObservableList<MenuItem> list) {
    final MenuItem item;
    if (selected != null) {
      final CheckMenuItem checkMenuItem = new CheckMenuItem();
      checkMenuItem.selectedProperty().bind(selected);
      item = checkMenuItem;
      if (eventHandler != null) {
        item.onActionProperty().bind(eventHandler);
      }
    } else if (children != null) {
      final Menu menu = new Menu();
      item = menu;
      menu.getItems().setAll(children.getValue().stream().map(c -> menuItem(menu.getItems())).collect(toList()));
    } else {
      item = new MenuItem();
      if (eventHandler != null) {
        item.onActionProperty().bind(eventHandler);
      }
    }

    if (disabled != null) {
      item.disableProperty().bind(disabled);
    }
    if (text != null) {
      item.textProperty().bind(text);
    }
    if (accelerator != null) {
      item.acceleratorProperty().bind(accelerator);
    }
    if (icon != null) {
      item.graphicProperty().bind(icon(16));
    }

    menus.put(item, list);

    return item;
  }

  public ButtonBase button(@Nonnull ObservableList<Node> nodes) {
    final ButtonBase button;

    if (selected != null) {
      final ToggleButton toggleButton = new ToggleButton();
      button = toggleButton;
      toggleButton.selectedProperty().bind(selected);
    } else {
      button = new Button();
    }
    button.setFocusTraversable(false);

    if (icon != null) {
      button.graphicProperty().bind(icon(20));
    }

    if (disabled != null) {
      button.disableProperty().bind(disabled);
    }

    if (text != null) {
      button.tooltipProperty().bind(createObjectBinding(() -> {
        final String v = text.getValue();
        return v == null ? null : new Tooltip(v);
      }, text));
    }

    button.setOnAction(event -> {
      if (children == null) {
        final EventHandler<ActionEvent> h = eventHandler.getValue();
        if (h != null) {
          h.handle(event);
        }
      } else {
        final ContextMenu contextMenu = grouped(children.getValue());
        contextMenu.show(button, Side.BOTTOM, 0, 0);
      }
    });

    buttons.put(button, nodes);

    return button;
  }

  public FxAction bindChildren(ObservableValue<ObservableList<FxAction>> actions) {
    children = set(actions, children);
    return this;
  }

  public boolean isDisabled() {
    return disabled == null || disabled.getValue() == null ? false : disabled.getValue();
  }

  public EventHandler<ActionEvent> getEventHandler() {
    return eventHandler == null ? null : eventHandler.getValue();
  }

  @Override
  public String toString() {
    return String.format("%s,%s,%s", group, toolbarGroup, menu);
  }
}
