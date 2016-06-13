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
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.jboss.logmanager.LogManager;
import org.marid.ide.panes.logging.IdeLogHandler;
import org.marid.ide.scenes.IdeScene;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.util.Utils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.stream.IntStream.of;
import static javafx.scene.paint.Color.GREEN;
import static org.marid.jfx.FxMaridIcon.maridIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application implements L10nSupport, LogSupport, PrefSupport {

    public static final Preferences PREFERENCES = PrefUtils.preferences(Ide.class);
    public static final Image[] IMAGES = of(16, 24, 32).mapToObj(n -> maridIcon(n, GREEN)).toArray(Image[]::new);

    static AnnotationConfigApplicationContext context;
    static Ide application;
    static IdeLogHandler ideLogHandler;

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
        Application.setUserAgentStylesheet(getPref("style", STYLESHEET_MODENA));
        final IdeScene ideScene = context.getBean(IdeScene.class);
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle(s("Marid IDE"));
        primaryStage.setScene(ideScene);
        primaryStage.getIcons().addAll(IMAGES);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            context.close();
            Logger.getLogger("").removeHandler(ideLogHandler);
        } finally {
            ideLogHandler = null;
            application = null;
        }
    }

    public static void main(String... args) throws Exception {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        Logger.getLogger("").addHandler(ideLogHandler = new IdeLogHandler());
        Utils.merge(System.getProperties(), "meta.properties", "ide.properties");
        final String localeString = PREFERENCES.get("locale", "");
        if (!localeString.isEmpty()) {
            Locale.setDefault(Locale.forLanguageTag(localeString));
        }
        disableUrlCaching();
        launch(Ide.class, args);
    }

    private static void disableUrlCaching() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource("ide.properties");
        if (url != null) {
            final URLConnection connection = url.openConnection();
            connection.setDefaultUseCaches(false);
        }
    }

    static AnnotationConfigApplicationContext child(Class<?> type) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setDisplayName(type.getName());
        context.setParent(Ide.context);
        context.scan(type.getPackage().getName());
        context.refresh();
        context.start();
        return context;
    }

    public static <T extends Window> T newWindow(Class<T> type) {
        final AnnotationConfigApplicationContext context = child(type);
        final T window = context.getBean(type);
        window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> context.close());
        return window;
    }

    public static <T extends Dialog<?>> T newDialog(Class<T> type) {
        final AnnotationConfigApplicationContext context = child(type);
        final T dialog = context.getBean(type);
        dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                context.close();
            }
        });
        return dialog;
    }
}
