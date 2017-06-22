/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.MotionBlur;
import org.marid.ide.panes.main.IdeStatusBar;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.function.Consumer;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IdeService<V extends Node> extends Service<Duration> {

    private final SimpleObjectProperty<V> graphic = new SimpleObjectProperty<>();
    private Button button;

    @Autowired
    private void init(IdeStatusBar statusBar) {
        addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, event -> {
            button = new Button();
            button.textProperty().bind(titleProperty());
            button.graphicProperty().bind(graphic);
            button.tooltipProperty().bind(createObjectBinding(() -> new Tooltip(getMessage()), messageProperty()));
            statusBar.add(button);
        });
        addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
            statusBar.remove(button);
            button = null;
            n(INFO, "{0} succeeded in {1}", event.getSource().getTitle(), event.getSource().getValue());
        });
        addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> {
            statusBar.remove(button);
            button = null;
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
            final long startTime = System.nanoTime();
            {
                final V node = createGraphic();
                final ContextMenu contextMenu = contextMenu();
                final MenuItem cancelItem = new MenuItem();
                cancelItem.setGraphic(FontIcons.glyphIcon("D_CLOSE_CIRCLE"));
                cancelItem.textProperty().bind(ls("Cancel"));
                cancelItem.setOnAction(event -> cancel());
                Platform.runLater(() -> {
                    graphic.set(node);
                    button.setContextMenu(contextMenu);
                });
            }
            try {
                execute();
            } finally {
                Platform.runLater(() -> graphic.set(null));
            }
            return Duration.ofNanos(System.nanoTime() - startTime);
        }
    }
}
