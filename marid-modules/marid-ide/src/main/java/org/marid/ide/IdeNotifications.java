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

package org.marid.ide;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.jfx.icons.FontIcons;
import org.marid.misc.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class IdeNotifications {

  public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                       @Nonnull String message,
                       @Nonnull Object... args) {
    log(4, level, message, null, args);
    n0(level, message, null, null, args);
  }

  public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                       @Nonnull String message,
                       @Nullable Parent details,
                       @Nonnull Object... args) {
    log(4, level, message, null, args);
    n0(level, message, details, null, args);
  }

  public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                       @Nonnull String message,
                       @Nullable Throwable thrown,
                       @Nonnull Object... args) {
    log(4, level, message, thrown, args);
    n0(level, message, null, thrown, args);
  }

  public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                       @Nonnull String message,
                       @Nullable Parent details,
                       @Nullable Throwable thrown,
                       @Nonnull Object... args) {
    log(4, level, message, thrown, args);
    n0(level, message, details, thrown, args);
  }

  private static void n0(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                         @Nonnull String message,
                         @Nullable Parent details,
                         @Nullable Throwable thrown,
                         @Nonnull Object... args) {
    final String text = m(Locale.getDefault(), message, args);
    final List<Action> actions = new ArrayList<>();
    if (details != null) {
      final Action action = new Action(actionEvent -> {
        details.setDisable(false);
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(details));
        stage.setResizable(true);
        stage.titleProperty().bind(ls("Details"));
        stage.show();
      });
      action.textProperty().bind(ls("Details"));
      action.setGraphic(FontIcons.glyphIcon("D_DETAILS", 20));
      actions.add(action);
    }
    if (thrown != null) {
      final Action action = new Action(event -> {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        final TextArea textArea = new TextArea(StringUtils.throwableText(thrown));
        textArea.setFont(new Font("Monospace", 12));
        stage.setScene(new Scene(textArea));
        stage.setResizable(true);
        stage.titleProperty().bind(ls("Error text"));
        stage.show();
      });
      action.textProperty().bind(ls("Error text"));
      action.setGraphic(FontIcons.glyphIcon("D_PANORAMA_WIDE_ANGLE", 20));
      actions.add(action);
    }
    final Notifications notifications = Notifications.create()
        .text(text)
        .title(s(level.getName()))
        .action(actions.toArray(new Action[actions.size()]))
        .darkStyle()
        .position(Pos.TOP_RIGHT);
    final Runnable task = () -> {
      notifications.owner(Ide.primaryStage);
      switch (level.getName()) {
        case "INFO":
          notifications.showInformation();
          break;
        case "ERROR":
        case "SEVERE":
          notifications.showError();
          break;
        case "WARNING":
          notifications.showWarning();
          break;
        case "CONFIG":
          notifications.showConfirm();
          break;
        default:
          notifications.show();
          break;
      }
    };
    if (Platform.isFxApplicationThread()) {
      task.run();
    } else {
      Platform.runLater(task);
    }
  }
}
