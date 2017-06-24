/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
