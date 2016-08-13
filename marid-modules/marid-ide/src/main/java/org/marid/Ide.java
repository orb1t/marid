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

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jboss.logmanager.LogManager;
import org.marid.ide.logging.IdeConsoleLogHandler;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.preloader.IdePreloader;
import org.marid.spring.postprocessors.LogBeansPostProcessor;
import org.marid.spring.postprocessors.OrderedInitPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.stream.IntStream.of;
import static javafx.scene.paint.Color.GREEN;
import static org.marid.jfx.FxMaridIcon.maridIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    public static Stage primaryStage;
    public static Ide ide;
    public static Logger rootLogger;

    static IdeLogHandler ideLogHandler;

    @Override
    public void init() throws Exception {
        Ide.ide = this;
        rootLogger.addHandler(ideLogHandler = new IdeLogHandler());
        context.setDisplayName(Ide.class.getName());
        context.setAllowBeanDefinitionOverriding(false);
        context.setAllowCircularReferences(false);
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.register(IdeContext.class);
        context.getBeanFactory().addBeanPostProcessor(new OrderedInitPostProcessor(context));
        context.getBeanFactory().addBeanPostProcessor(new LogBeansPostProcessor());
        context.refresh();
        context.start();
        context.getBean(IdePane.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Ide.primaryStage = primaryStage;
        final IdePane idePane = context.getBean(IdePane.class);
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle("Marid IDE");
        primaryStage.setScene(new Scene(idePane, 1024, 768));
        primaryStage.getIcons().addAll(of(16, 24, 32).mapToObj(n -> maridIcon(n, GREEN)).toArray(Image[]::new));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }

    public static void main(String... args) throws Exception {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.parse(IdePrefs.PREFERENCES.get("logLevel", Level.INFO.getName())));
        rootLogger.addHandler(new IdeConsoleLogHandler());
        for (final String resource : new String[] {"meta.properties", "ide.properties"}) {
            final Enumeration<URL> e = currentThread().getContextClassLoader().getResources(resource);
            while (e.hasMoreElements()) {
                try (final InputStream inputStream = e.nextElement().openStream()) {
                    System.getProperties().load(inputStream);
                }
            }
        }
        final String localeString = IdePrefs.PREFERENCES.get("locale", "");
        if (!localeString.isEmpty()) {
            Locale.setDefault(Locale.forLanguageTag(localeString));
        }
        if ("true".equalsIgnoreCase(System.getProperty("ide.preloader.disabled"))) {
            launch(Ide.class, args);
        } else {
            LauncherImpl.launchApplication(Ide.class, IdePreloader.class, args);
        }
    }
}
