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

package org.marid;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.image.MaridIconFx;
import org.marid.jfx.list.MaridListActions;
import org.marid.jfx.logging.LogComponent;
import org.marid.spring.postprocessors.IdeAppContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;

import static org.marid.IdePrefs.PREFERENCES;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

    public static Ide ide;
    public static Stage primaryStage;

    private volatile ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        ide = this;
        final String[] args = getParameters().getRaw().toArray(new String[0]);
        new Thread(() -> {
            final SpringApplication application = new SpringApplication(IdeContext.class);
            application.setApplicationContextClass(IdeAppContext.class);
            context = application.run(args);
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Ide.primaryStage = primaryStage;

        final LogComponent logComponent = new LogComponent(IdeLogHandler.LOG_RECORDS);
        final Runnable autoscrollUnregisterer = MaridListActions.autoScroll(logComponent);
        final Stage splash = new Stage(StageStyle.UNDECORATED);
        splash.setTitle("Marid IDE");
        splash.getIcons().addAll(MaridIconFx.getIcons(24, 32));
        splash.setScene(new Scene(logComponent, 600, 600));
        splash.show();

        final Thread contextStartThread = new Thread(() -> {
            while (context == null) {
                LockSupport.parkNanos(100_000_000L);
            }
            Platform.runLater(() -> {
                try {
                    context.start();
                    primaryStage.setMinWidth(750.0);
                    primaryStage.setMinHeight(550.0);
                    primaryStage.setTitle("Marid IDE");
                    primaryStage.setScene(new Scene(context.getBean(IdePane.class), 1024, 768));
                    primaryStage.getIcons().addAll(MaridIconFx.getIcons(24, 32));
                    primaryStage.setMaximized(true);
                    splash.hide();
                    setUserAgentStylesheet(PREFERENCES.get("style", STYLESHEET_MODENA));
                    primaryStage.show();
                } finally {
                    autoscrollUnregisterer.run();
                }
            });
        });
        contextStartThread.setDaemon(true);
        contextStartThread.start();
    }

    @Override
    public void stop() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String... args) throws Exception {
        // Desktop initialization
        Desktop.isDesktopSupported();

        // locale
        final String locale = PREFERENCES.get("locale", null);
        if (locale != null) {
            Locale.setDefault(Locale.forLanguageTag(locale));
        }

        // launch application
        Application.launch(args);
    }
}
