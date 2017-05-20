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

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.marid.ide.status.IdeStatusBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class IdePane extends BorderPane {

    @Autowired
    private void center(IdeSplitPane pane) {
        setCenter(pane);
    }

    @Autowired
    private void top(IdeMenu ideMenu, IdeToolbar toolbar) {
        final BorderPane menuPane = new BorderPane();
        menuPane.setCenter(ideMenu);
        setTop(new VBox(menuPane, toolbar));
    }

    @Autowired
    private void bottom(IdeStatusBar statusBar) {
        setBottom(statusBar);
    }
}
