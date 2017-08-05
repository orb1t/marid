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

package org.marid;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.image.MaridIconFx;
import org.marid.splash.MaridSplash;
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
            context.getBean(IdePane.class);
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Ide.primaryStage = primaryStage;

        final MaridSplash maridSplash = new MaridSplash(IdeLogHandler.LOG_RECORDS);

        final Stage splash = new Stage(StageStyle.UNDECORATED);
        splash.setTitle("Marid");
        splash.getIcons().addAll(MaridIconFx.getIcons(24, 32));
        splash.setScene(new Scene(maridSplash));
        splash.show();
        maridSplash.init();

        setUserAgentStylesheet(PREFERENCES.get("style", STYLESHEET_MODENA));

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
                    primaryStage.show();
                } finally {
                    maridSplash.close();
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
