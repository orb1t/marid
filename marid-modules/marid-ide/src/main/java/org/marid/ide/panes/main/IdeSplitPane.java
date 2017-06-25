/*
 *
 */

package org.marid.ide.panes.main;

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
