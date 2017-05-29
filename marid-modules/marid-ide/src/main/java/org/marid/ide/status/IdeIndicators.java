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

package org.marid.ide.status;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Separator;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Service
@Lazy(false)
public class IdeIndicators {

    private final List<Callable<Runnable>> updateTasks = new ArrayList<>();
    private final IdeStatusBar statusBar;

    @Autowired
    public IdeIndicators(IdeStatusBar statusBar) {
        this.statusBar = statusBar;
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
        add(combo);
    }

    @Order(2)
    @Autowired
    public void initDateTime() throws Exception {
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
        updateTasks.add(() -> {
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            final String time = now.format(timeFormatter);
            return () -> timeLabel.setText(time);
        });
        timeLabel.setMaxHeight(Double.MAX_VALUE);
        add(timeLabel);
    }

    private void add(Node... nodes) {
        if (!statusBar.getChildren().isEmpty()) {
            statusBar.getChildren().add(new Separator(Orientation.VERTICAL));
        }
        statusBar.getChildren().addAll(nodes);
    }

    @Scheduled(fixedDelay = 1_000L)
    private void update() throws Exception {
        for (final Callable<Runnable> task : updateTasks) {
            Platform.runLater(task.call());
        }
    }
}
