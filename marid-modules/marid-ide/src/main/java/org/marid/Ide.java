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
import org.jboss.logmanager.LogManager;
import org.marid.ide.logging.IdeConsoleLogHandler;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.io.UrlConnection;
import org.marid.misc.Props;
import org.marid.pref.PrefUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.stream.IntStream.of;
import static javafx.scene.paint.Color.GREEN;
import static org.marid.jfx.FxMaridIcon.maridIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

    public static final Preferences PREFERENCES = PrefUtils.preferences(Ide.class);
    public static final Image[] IMAGES = of(16, 24, 32).mapToObj(n -> maridIcon(n, GREEN)).toArray(Image[]::new);

    static AnnotationConfigApplicationContext context;
    static Ide application;

    @Override
    public void init() throws Exception {
        context = new AnnotationConfigApplicationContext();
        context.setDisplayName(Ide.class.getName());
        context.setAllowCircularReferences(false);
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setResourceLoader(new PathMatchingResourcePatternResolver(context.getClassLoader()));
        context.register(IdeContext.class);
        application = this;
        context.refresh();
        context.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(PREFERENCES.get("style", STYLESHEET_MODENA));
        final IdePane idePane = context.getBean(IdePane.class);
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle("Marid IDE");
        primaryStage.setScene(new Scene(idePane, 1024, 768));
        primaryStage.getIcons().addAll(IMAGES);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            context.close();
            final Logger logger = Logger.getLogger("");
            for (final Handler handler : logger.getHandlers()) {
                try {
                    handler.close();
                } catch (Exception x) {
                    x.printStackTrace(System.err);
                }
                logger.removeHandler(handler);
            }
        } finally {
            application = null;
            context = null;
        }
    }

    public static void main(String... args) throws Exception {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        Optional.ofNullable(Logger.getLogger("")).ifPresent(logger -> {
            logger.setLevel(Level.parse(PREFERENCES.get("logLevel", Level.INFO.getName())));
            logger.addHandler(new IdeLogHandler());
            logger.addHandler(new IdeConsoleLogHandler());
        });
        Props.merge(System.getProperties(), "meta.properties", "ide.properties");
        final String localeString = PREFERENCES.get("locale", "");
        if (!localeString.isEmpty()) {
            Locale.setDefault(Locale.forLanguageTag(localeString));
        }
        new UrlConnection(null, null).setDefaultUseCaches(false);
        launch(Ide.class, args);
    }
}
