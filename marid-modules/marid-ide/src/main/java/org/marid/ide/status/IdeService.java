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

package org.marid.ide.status;

import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.marid.ide.panes.main.IdeStatusBar;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IdeService<V extends Node> extends Service<Duration> {

    protected static final Set<EventType<?>> DONE_EVENT_TYPES = ImmutableSet.of(
            WorkerStateEvent.WORKER_STATE_SUCCEEDED,
            WorkerStateEvent.WORKER_STATE_FAILED,
            WorkerStateEvent.WORKER_STATE_CANCELLED
    );

    private final SimpleObjectProperty<V> graphic = new SimpleObjectProperty<>();
    private final Label label = new Label();
    private final ProgressBar progressBar = new ProgressBar();
    protected final HBox button = new HBox(5, label);

    protected IdeStatusBar statusBar;

    @PostConstruct
    private void init() {
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPadding(new Insets(4));
        button.getStyleClass().add("button");
        button.setFocusTraversable(false);

        label.textProperty().bind(titleProperty());
        label.graphicProperty().bind(graphic);

        progressBar.setVisible(false);
        progressBar.setPrefWidth(50);
        progressBar.setPrefHeight(24);

        progressProperty().addListener((o, oV, nV) -> {
            if (!progressBar.isVisible()) {
                button.getChildren().add(button.getChildren().size() - 1, progressBar);
                progressBar.setVisible(true);
            }
            if (nV.doubleValue() <= oV.doubleValue() || nV.doubleValue() < 0.01) {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            } else {
                progressBar.setProgress(nV.doubleValue());
            }
        });

        messageProperty().addListener((o, oV, nV) -> {
            if (nV != null) {
                if (label.getTooltip() == null) {
                    label.setTooltip(new Tooltip());
                }
                label.getTooltip().setText(nV);
            } else {
                label.setTooltip(null);
            }
        });
    }

    @Autowired
    private void init(IdeStatusBar statusBar) {
        this.statusBar = statusBar;
        addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, event -> statusBar.add(button));
        addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            statusBar.remove(button);
            final Duration duration = (Duration) event.getSource().getValue();
            final String durationText = DurationFormatUtils.formatDurationHMS(duration.toMillis());
            n(INFO, "{0} succeeded in {1}", event.getSource().getTitle(), durationText);
        });
        addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> {
            statusBar.remove(button);
            n(WARNING, "{0} failed", event.getSource().getException(), event.getSource().getTitle());
        });
        addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event -> {
            button.setDisable(true);
            button.setEffect(new MotionBlur(0.1, 0.1));
        });
    }

    @Override
    protected abstract IdeTask createTask();

    protected abstract class IdeTask extends Task<Duration> {

        protected abstract void execute() throws Exception;

        @Nonnull
        protected abstract V createGraphic();

        protected abstract ContextMenu contextMenu();

        protected void updateGraphic(Consumer<V> consumer) {
            if (Platform.isFxApplicationThread()) {
                consumer.accept(graphic.get());
            } else {
                Platform.runLater(() -> consumer.accept(graphic.get()));
            }
        }

        @Override
        protected Duration call() throws Exception {
            try {
                final long startTime = System.nanoTime();
                {
                    final V node = createGraphic();
                    final ContextMenu contextMenu = contextMenu();
                    Platform.runLater(() -> {
                        graphic.set(node);
                        label.setContextMenu(contextMenu);

                        final Button cancel = new Button();
                        cancel.setOnAction(event -> {
                            cancel();
                            button.getChildren().remove(cancel);
                        });
                        final Tooltip tooltip = new Tooltip();
                        tooltip.textProperty().bind(ls("Cancel"));
                        cancel.setTooltip(tooltip);
                        cancel.setGraphic(FontIcons.glyphIcon("D_CLOSE_CIRCLE", 16));
                        button.getChildren().add(cancel);
                    });
                }
                try {
                    execute();
                } finally {
                    Platform.runLater(() -> graphic.set(null));
                }
                return Duration.ofNanos(System.nanoTime() - startTime);
            } finally {
                Platform.runLater(() -> statusBar.remove(button));
            }
        }
    }
}
