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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.context.annotation.Bean;
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
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.Comparator.comparingInt;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.ide.logging.IdeLogHandler.LOG_RECORDS;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.jfx.icons.IconFactory.icon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeStatusBar extends BorderPane {

    private final HBox right;
    private final HBox toolBar;

    public IdeStatusBar() {
        final ScrollPane scrollPane = new ScrollPane(toolBar = new HBox(5));
        setCenter(scrollPane);
        setRight(right = new HBox(10));

        setMargin(toolBar, new Insets(0, 5, 0, 5));
        setMargin(right, new Insets(0, 5, 0, 5));

        right.setAlignment(Pos.CENTER_RIGHT);

        toolBar.setFillHeight(true);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        scrollPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }

    @Order(1)
    @Bean(initMethod = "run")
    public Runnable healthInitializer(IdeSplitPane pane) {
        return () -> {
            final ObjectProperty<Level> lastLevel = new SimpleObjectProperty<>(LOG_RECORDS.stream()
                    .map(LogRecord::getLevel)
                    .max(comparingInt(Level::intValue))
                    .orElse(Level.INFO));
            final ToggleButton button = new ToggleButton();
            button.graphicProperty().bind(createObjectBinding(() -> icon(lastLevel.get(), 20), lastLevel));
            LOG_RECORDS.addListener((ListChangeListener<LogRecord>) c -> {
                while (c.next()) {
                    for (final LogRecord record : c.getAddedSubList()) {
                        if (record.getLevel().intValue() > lastLevel.get().intValue()) {
                            lastLevel.set(record.getLevel());
                        }
                    }
                }
            });
            button.setFocusTraversable(false);
            button.selectedProperty().addListener((o, oV, nV) -> {
                if (nV) {
                    lastLevel.set(Level.INFO);
                    pane.setPinnedSide(Side.BOTTOM);
                } else {
                    pane.setPinnedSide(null);
                }
            });
            right.getChildren().add(button);
        };
    }

    @Order(2)
    @Bean(initMethod = "run")
    public Runnable profileInitializer(ProjectManager manager) {
        return () -> {
            final ComboBox<ProjectProfile> combo = new ComboBox<>(manager.getProfiles());
            combo.setMinHeight(30);
            final SelectionModel<ProjectProfile> selection = combo.getSelectionModel();
            selection.select(manager.getProfile());
            final ObjectProperty<ProjectProfile> profile = manager.profileProperty();
            profile.addListener((observable, oldValue, newValue) -> selection.select(newValue));
            selection.selectedItemProperty().addListener((observable, oldValue, newValue) -> profile.set(newValue));
            right.getChildren().add(combo);
            HBox.setMargin(combo, new Insets(5));
        };
    }

    @Order(3)
    @Bean(initMethod = "run")
    public Runnable dateTimeInitializer(ScheduledExecutorService timer) {
        return () -> {
            final Label timeLabel = new Label("", glyphIcon("O_CLOCK", 18));
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
            HBox.setMargin(timeLabel, new Insets(5));
        };
    }

    public void add(Node button) {
        toolBar.getChildren().add(button);
        HBox.setMargin(button, new Insets(2));
    }

    public void remove(Node button) {
        toolBar.getChildren().remove(button);
    }
}
