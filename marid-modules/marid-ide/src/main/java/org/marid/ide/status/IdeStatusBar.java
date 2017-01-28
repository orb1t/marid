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

package org.marid.ide.status;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeStatusBar extends GridPane {

    private final Label label = new Label();
    private final IdeStatusTimer timer;
    private final IdeStatusProfile profile;

    @Autowired
    public IdeStatusBar(IdeStatusTimer timer, IdeStatusProfile profile) {
        addRow(0, label, separator(), this.profile = profile, separator(), this.timer = timer);
        setHgrow(label, Priority.SOMETIMES);
    }

    private Separator separator() {
        final Separator separator = new Separator(Orientation.VERTICAL);
        separator.setMinWidth(10.0);
        return separator;
    }

    public void setText(String text) {
        if (Platform.isFxApplicationThread()) {
            label.setText(text);
        } else {
            Platform.runLater(() -> label.setText(text));
        }
    }
}
