/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.marid.l10n.L10n;

/**
 * @author Dmitry Ovchinnikov.
 */
public class FxIde extends Application {

    public FxIde() {
        setUserAgentStylesheet(Application.STYLESHEET_MODENA);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(new Menu("File"));
        final VBox vBox = new VBox(menuBar, new Button("a"));
        final FxIdeScene scene = new FxIdeScene(vBox, 600, 400);
        stage.setScene(scene);
        stage.setTitle(L10n.s("Marid IDE"));
        stage.sizeToScene();
        stage.show();
    }

    public static void launch(String... args) {
        Application.launch(FxIde.class, args);
    }
}
