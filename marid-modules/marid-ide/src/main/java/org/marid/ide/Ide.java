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

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jboss.logmanager.LogManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.marid.ide.panes.logging.IdeLogHandler;
import org.marid.ide.scenes.IdeScene;
import org.marid.jfx.Configurable;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.util.Utils;

import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static javafx.scene.paint.Color.GREEN;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import static org.marid.jfx.FxMaridIcon.maridIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application implements L10nSupport, LogSupport, Configurable {

    private final Weld weld = new Weld(getClass().getName());
    private final WeldContainer container = weld.initialize();

    @Override
    public void init() throws Exception {
        Application.setUserAgentStylesheet(getPref("style", STYLESHEET_CASPIAN));
        final String css = getPref(String.class, "css", null);
        if (css != null) {
            StyleManager.getInstance().addUserAgentStylesheet(css);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initBounds(primaryStage);
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle(s("Marid IDE"));
        primaryStage.setScene(container.select(IdeScene.class).get());
        primaryStage.addEventHandler(WINDOW_CLOSE_REQUEST, e -> weld.shutdown());
        primaryStage.getIcons().addAll(IntStream.of(16, 24, 32)
                .mapToObj(n -> maridIcon(n, GREEN))
                .toArray(Image[]::new));
        primaryStage.show();
    }

    public static void main(String... args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        Logger.getLogger("").addHandler(new IdeLogHandler());
        Utils.merge(System.getProperties(), "meta.properties", "ide.properties");
        Application.launch(Ide.class, args);
    }
}