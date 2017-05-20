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

package org.marid.ide.panes.main;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.stage.WindowEvent;
import org.marid.ide.logging.IdeLogPane;
import org.marid.ide.tabs.IdeTabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

import static javafx.stage.WindowEvent.WINDOW_SHOWN;
import static org.marid.Ide.primaryStage;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class IdeSplitPane extends SplitPane {

    private static final double DEFAULT_POSITION = 0.9;

    private final IdeTabPane tabPane;
    private final IdeLogPane ideLogPane;
    private final Divider divider;
    private final Preferences preferences;

    @Autowired
    public IdeSplitPane(IdeTabPane tabPane, IdeLogPane ideLogPane, Preferences preferences) {
        super(tabPane, ideLogPane);
        this.tabPane = tabPane;
        this.ideLogPane = ideLogPane;
        this.preferences = preferences;
        this.divider = getDividers().get(0);
        setOrientation(Orientation.VERTICAL);
        setDividerPositions(0);
        setFocusTraversable(false);
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        if (primaryStage.isShowing()) {
            applyDivider();
        } else {
            primaryStage.addEventHandler(WINDOW_SHOWN, this::onShow);
        }
    }

    private void applyDivider() {
        final double dividerPos = preferences.getDouble("divider", DEFAULT_POSITION);
        divider.setPosition(dividerPos);
        divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == DEFAULT_POSITION) {
                preferences.remove("divider");
            } else {
                preferences.putDouble("divider", newValue.doubleValue());
            }
        });
    }

    private void onShow(WindowEvent event) {
        primaryStage.removeEventHandler(WINDOW_SHOWN, this::onShow);
        applyDivider();
    }
}
