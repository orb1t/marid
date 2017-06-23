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

package org.marid.ide;

import javafx.application.Platform;
import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.Ide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.logging.Level;

import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class IdeNotifications  {

    public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                         @Nonnull String message,
                         @Nonnull Object... args) {
        log(4, level, message, null, args);
        n0(level, message, null, args);
    }

    public static void n(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                         @Nonnull String message,
                         @Nullable Throwable thrown,
                         @Nonnull Object... args) {
        log(4, level, message, thrown, args);
        n0(level, message, thrown, args);
    }

    private static void n0(@Nonnull @MagicConstant(valuesFromClass = Level.class) Level level,
                           @Nonnull String message,
                           @Nullable Throwable thrown,
                           @Nonnull Object... args) {
        final String text = m(Locale.getDefault(), message, args);
        final Notifications notifications = Notifications.create()
                .text(text)
                .title(s(level.getName()))
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
