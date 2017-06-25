/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.panes.main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static javafx.beans.binding.Bindings.size;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeStatusBar extends BorderPane {

    private final HBox right;
    private final ScrollPane scrollPane;
    private final HBox toolBar;
    private final Button notificationsButton;
    private final ObservableList<Node> notifications = FXCollections.observableArrayList();
    private final ContextMenu contextMenu = new ContextMenu();

    public IdeStatusBar() {
        setPadding(new Insets(5, 5, 5, 5));
        setFocusTraversable(false);

        setCenter(scrollPane = new ScrollPane(toolBar = new HBox(5)));
        setRight(right = new HBox(10));

        setMargin(toolBar, new Insets(0, 5, 0, 5));
        setMargin(right, new Insets(0, 5, 0, 5));

        right.setAlignment(Pos.CENTER_RIGHT);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        right.getChildren().add(notificationsButton = new Button());
        final Circle circle = new Circle(24);
        notificationsButton.setShape(circle);
        notificationsButton.textProperty().bind(size(notifications).asString());
        notificationsButton.disableProperty().bind(Bindings.isEmpty(notifications));
        final Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(ls("Notifications"));
        notificationsButton.setOnAction(event -> contextMenu.show(notificationsButton, Side.TOP, 0, 0));

        {
            final ContextMenu buttonContextMenu = new ContextMenu();
            final MenuItem clearAllNotifications = new MenuItem(null, FontIcons.glyphIcon("D_CLOSE"));
            clearAllNotifications.textProperty().bind(ls("Remove all notifications"));
            clearAllNotifications.setOnAction(event -> notifications.clear());
            buttonContextMenu.getItems().add(clearAllNotifications);
            notificationsButton.setContextMenu(buttonContextMenu);
        }
    }

    @Order(1)
    @Autowired
    public void initProfile(ProjectManager manager) {
        final ComboBox<ProjectProfile> combo = new ComboBox<>(manager.getProfiles());
        final SelectionModel<ProjectProfile> selection = combo.getSelectionModel();
        selection.select(manager.getProfile());
        final ObjectProperty<ProjectProfile> profile = manager.profileProperty();
        profile.addListener((observable, oldValue, newValue) -> selection.select(newValue));
        selection.selectedItemProperty().addListener((observable, oldValue, newValue) -> profile.set(newValue));
        right.getChildren().add(combo);
    }

    @Order(2)
    @Autowired
    public void initDateTime(ScheduledExecutorService timer) throws Exception {
        final Label timeLabel = new Label("", glyphIcon("O_CLOCK", 16));
        final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral(' ')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();
        timeLabel.setMaxHeight(Double.MAX_VALUE);
        timer.scheduleWithFixedDelay(() -> {
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            final String time = now.format(timeFormatter);
            Platform.runLater(() -> timeLabel.setText(time));
        }, 1_000L, 1_000L, TimeUnit.MILLISECONDS);
        right.getChildren().add(timeLabel);
    }

    public void add(Button button) {
        toolBar.getChildren().add(button);
        HBox.setMargin(button, new Insets(0, 5, 0, 5));
    }

    public void remove(Button button) {
        toolBar.getChildren().remove(button);
    }

    public void addNotification(ObservableValue<String> text, Node node) {
        notifications.add(node);

        final MenuItem menuItem = new MenuItem();
        final Button close = new Button(null, FontIcons.glyphIcon("D_CLOSE_CIRCLE"));
        close.setOnAction(event -> {
            contextMenu.getItems().remove(menuItem);
            notifications.remove(node);
            contextMenu.hide();
        });
        menuItem.textProperty().bind(text);
        menuItem.setGraphic(close);
        menuItem.setOnAction(event -> {
            final PopOver popOver = new PopOver(node);
            popOver.setHideOnEscape(true);
            popOver.setHeaderAlwaysVisible(true);
            popOver.titleProperty().bind(text);
            popOver.setArrowLocation(ArrowLocation.RIGHT_BOTTOM);
            popOver.setAutoHide(false);

            final ChangeListener<ContextMenu> menuChangeListener = (o, oV, nV) -> {
                if (nV == null) {
                    popOver.hide();
                }
            };
            popOver.setOnHiding(e -> {
                menuItem.parentPopupProperty().removeListener(menuChangeListener);
                menuItem.setDisable(false);
            });
            popOver.setOnShowing(e -> menuItem.setDisable(true));
            menuItem.parentPopupProperty().addListener(menuChangeListener);

            popOver.show(notificationsButton);
        });

        contextMenu.getItems().add(0, menuItem);

        final ListChangeListener<Node> changeListener = c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (final Node n : c.getRemoved()) {
                        if (n == node) {
                            contextMenu.getItems().remove(menuItem);
                            break;
                        }
                    }
                }
            }
        };
        menuItem.setUserData(changeListener);
        notifications.addListener(new WeakListChangeListener<>(changeListener));
    }
}
