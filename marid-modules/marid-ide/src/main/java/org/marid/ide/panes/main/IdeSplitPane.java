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
import org.marid.ide.logging.IdeLogPane;
import org.marid.ide.tabs.IdeTabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

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

    @Autowired
    public IdeSplitPane(IdeTabPane tabPane, IdeLogPane ideLogPane) {
        super(tabPane, ideLogPane);
        this.tabPane = tabPane;
        this.ideLogPane = ideLogPane;
        this.divider = getDividers().get(0);
        setOrientation(Orientation.VERTICAL);
    }

    @Autowired
    private void initDivider(Preferences preferences) {
        divider.setPosition(preferences.getDouble("divider", DEFAULT_POSITION));
        final PreferenceChangeListener preferenceChangeListener = evt -> {
            final String v = evt.getNewValue();
            switch (evt.getKey()) {
                case "divider":
                    if (v == null) {
                        divider.setPosition(DEFAULT_POSITION);
                    } else {
                        divider.setPosition(Double.parseDouble(v));
                    }
                    break;
            }
        };
        preferences.addPreferenceChangeListener(preferenceChangeListener);
        divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            preferences.removePreferenceChangeListener(preferenceChangeListener);
            if (newValue.doubleValue() == DEFAULT_POSITION) {
                preferences.remove("divider");
            } else {
                preferences.putDouble("divider", DEFAULT_POSITION);
            }
            try {
                preferences.sync();
            } catch (Exception x) {
                
            }
            preferences.addPreferenceChangeListener(preferenceChangeListener);
        });
    }
}
