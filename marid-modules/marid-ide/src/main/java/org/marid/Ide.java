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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.marid.IdeDependants.MainContext;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.image.MaridIconFx;
import org.marid.spring.postprocessors.MaridCommonPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Locale;
import java.util.stream.IntStream;

import static org.marid.IdePrefs.PREFERENCES;
import static org.marid.ide.logging.IdeLogConfig.ROOT_LOGGER;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

    private final AnnotationConfigApplicationContext context = new MainContext();

    public static Stage primaryStage;
    public static Ide ide;
    public static final IdeLogHandler ideLogHandler;

    @Override
    public void init() throws Exception {
        Ide.ide = this;
        context.setId("root");
        context.setDisplayName("Root Context");
        context.register(IdeContext.class);
        context.refresh();
        context.getBean(IdePane.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Ide.primaryStage = primaryStage;
        context.start();
        final IdePane idePane = context.getBean(IdePane.class);
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle("Marid IDE");
        primaryStage.setScene(new Scene(idePane, 1024, 768));
        primaryStage.setMaximized(true);
        primaryStage.getIcons().addAll(IntStream.of(24, 32).mapToObj(MaridIconFx::getIcon).toArray(Image[]::new));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }

    static {
        // console logger
        System.setProperty("java.util.logging.config.class", "org.marid.ide.logging.IdeLogConfig");

        // locale
        final String locale = PREFERENCES.get("locale", null);
        if (locale != null) {
            Locale.setDefault(Locale.forLanguageTag(locale));
        }

        MaridCommonPostProcessor.replaceInjectedMetadata();

        ROOT_LOGGER.addHandler(ideLogHandler = new IdeLogHandler());
    }
}
