/*-
 * #%L
 * marid-ide
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

package org.marid.ide.status;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static javafx.concurrent.WorkerStateEvent.*;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IdeService<V extends Node> extends Service<Duration> {

  protected static final Set<EventType<?>> DONE_EVENT_TYPES = Set.of(
      WORKER_STATE_SUCCEEDED,
      WORKER_STATE_FAILED,
      WORKER_STATE_CANCELLED
  );

  private final SimpleObjectProperty<V> graphic = new SimpleObjectProperty<>();
  protected final SimpleObjectProperty<Parent> details = new SimpleObjectProperty<>();
  private final ProgressBar progressBar = new ProgressBar();
  protected final IdeServiceButton button = new IdeServiceButton();

  private IdeStatusBar statusBar;
  private PopOver popOver;

  public IdeService() {
    button.setAlignment(Pos.CENTER_LEFT);
    button.setFocusTraversable(true);
    button.setOnMouseClicked(event -> onDetail());
    button.label.textProperty().bind(titleProperty());
    button.label.graphicProperty().bind(graphic);

    progressBar.setVisible(false);
    progressBar.setPrefWidth(50);
    progressBar.setPrefHeight(24);

    progressProperty().addListener((o, oV, nV) -> {
      if (!progressBar.isVisible()) {
        button.box.getChildren().add(button.box.getChildren().size() - 1, progressBar);
        progressBar.setVisible(true);
      }
      if (nV.doubleValue() <= oV.doubleValue() || nV.doubleValue() < 0.01) {
        progressBar.setProgress(INDETERMINATE_PROGRESS);
      } else {
        progressBar.setProgress(nV.doubleValue());
      }
    });

    messageProperty().addListener((o, oV, nV) -> {
      if (nV != null) {
        if (button.label.getTooltip() == null) {
          button.label.setTooltip(new Tooltip());
        }
        button.label.getTooltip().setText(nV);
      } else {
        button.label.setTooltip(null);
      }
    });
  }

  private void onDetail() {
    final Parent detailNode = details.get();
    if (detailNode != null && popOver == null) {
      final BorderPane pane = new BorderPane(detailNode);
      popOver = new PopOver(pane);
      popOver.setHideOnEscape(true);
      popOver.setCloseButtonEnabled(true);
      popOver.setHeaderAlwaysVisible(true);
      popOver.setAutoHide(false);
      popOver.titleProperty().bind(titleProperty());
      popOver.setArrowLocation(ArrowLocation.BOTTOM_LEFT);

      final ChangeListener<Boolean> runningListener = (o, oV, nV) -> {
        if (!nV) {
          popOver.setContentNode(null);
          popOver.hide();
        }
      };
      popOver.setOnHiding(event -> {
        pane.setCenter(null);
        popOver.setContentNode(null);
        runningProperty().removeListener(runningListener);
        popOver = null;
      });
      runningProperty().addListener(runningListener);

      popOver.show(button);
    } else if (popOver != null) {
      popOver.hide();
    }
  }

  @Autowired
  private void init(IdeStatusBar statusBar) {
    this.statusBar = statusBar;
    addEventHandler(WORKER_STATE_RUNNING, event -> statusBar.add(button));
    addEventHandler(WORKER_STATE_SUCCEEDED, event -> {
      statusBar.remove(button);
      final Duration duration = (Duration) event.getSource().getValue();
      final String durationText = DurationFormatUtils.formatDurationHMS(duration.toMillis());
      n(INFO, "{0} succeeded in {1}", details.get(), event.getSource().getTitle(), durationText);
    });
    addEventHandler(WORKER_STATE_FAILED, event -> {
      statusBar.remove(button);
      n(WARNING, "{0} failed", details.get(), event.getSource().getException(), event.getSource().getTitle());
    });
    addEventHandler(WORKER_STATE_CANCELLED, event -> button.setEffect(new SepiaTone(0.5)));
  }

  @Override
  protected abstract IdeTask createTask();

  protected abstract class IdeTask extends Task<Duration> {

    protected abstract void execute() throws Exception;

    @Nonnull
    protected abstract V createGraphic();

    protected ContextMenu contextMenu() {
      return null;
    }

    protected void updateGraphic(Consumer<V> consumer) {
      if (Platform.isFxApplicationThread()) {
        consumer.accept(graphic.get());
      } else {
        Platform.runLater(() -> consumer.accept(graphic.get()));
      }
    }

    @Override
    protected final Duration call() throws Exception {
      updateTitle(s(IdeService.this.getClass().getSimpleName()));
      try {
        final long startTime = System.nanoTime();
        {
          final V node = createGraphic();
          Platform.runLater(() -> {
            graphic.set(node);
            button.label.setContextMenu(contextMenu());

            final Button cancel = new Button();
            cancel.setOnAction(event -> {
              cancel();
              button.box.getChildren().remove(cancel);
            });
            final Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(ls("Cancel"));
            cancel.setTooltip(tooltip);
            cancel.setGraphic(FontIcons.glyphIcon("D_CLOSE_CIRCLE", 16));
            button.box.getChildren().add(cancel);
          });
        }
        execute();
        return Duration.ofNanos(System.nanoTime() - startTime);
      } finally {
        Platform.runLater(() -> {
          if (popOver != null) {
            popOver.hide();
          }
          graphic.set(null);
          statusBar.remove(button);
        });
      }
    }
  }
}
