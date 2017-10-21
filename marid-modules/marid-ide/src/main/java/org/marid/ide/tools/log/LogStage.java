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

package org.marid.ide.tools.log;

import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import org.marid.Ide;
import org.marid.IdePrefs;
import org.marid.spring.annotation.Initializer;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.spring.ui.FxInit;
import org.marid.spring.ui.ScopedStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.prefs.Preferences;

import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

@PrototypeComponent
public class LogStage extends ScopedStage {

    @Autowired
    public LogStage() {
        super(StageStyle.DECORATED);
        final Preferences preferences = IdePrefs.PREFERENCES.node("logs");
        initOwner(Ide.primaryStage);
        setOnCloseRequest(event -> {
            preferences.putDouble("x", getX());
            preferences.putDouble("y", getY());
            preferences.putDouble("width", getWidth());
            preferences.putDouble("height", getHeight());
        });
        setX(preferences.getDouble("x", getX()));
        setY(preferences.getDouble("y", getY()));
    }

    @Autowired
    private void init(@Qualifier("log") BorderPane logPane) {
        final Preferences preferences = IdePrefs.PREFERENCES.node("logs");
        setScene(new Scene(logPane, preferences.getDouble("width", 800), preferences.getDouble("height", 600)));
    }

    @Initializer(order = 1)
    public FxInit alwaysOnTopItem(@Qualifier("log") Menu actionsMenu) {
        return () -> {
            actionsMenu.getItems().add(new SeparatorMenuItem());
            final CheckMenuItem menuItem = new CheckMenuItem(s("Always on top"), glyphIcon("M_BORDER_TOP", 16));
            menuItem.setOnAction(event -> setAlwaysOnTop(!isAlwaysOnTop()));
            actionsMenu.getItems().add(menuItem);
        };
    }
}
