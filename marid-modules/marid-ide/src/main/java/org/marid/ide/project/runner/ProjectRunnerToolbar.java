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

package org.marid.ide.project.runner;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import org.marid.ide.icons.IdeIcons;
import org.marid.logging.LogSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectRunnerToolbar extends ToolBar implements LogSupport {

    private final ProjectRunnerPane pane;

    public ProjectRunnerToolbar(ProjectRunnerPane pane) {
        this.pane = pane;
        getItems().add(stopButton());
        pane.out.getItems().addListener((Change<? extends String> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    if (c.getAddedSubList().stream().filter("###Closed###"::equals).findAny().isPresent()) {
                        getItems().forEach(n -> n.setDisable(true));
                    }
                }
            }
        });
    }

    private Button stopButton() {
        final Button button = new Button(null, IdeIcons.glyphIcon(MaterialIcon.STOP));
        button.setOnAction(event -> pane.printStream.println("exit"));
        return button;
    }
}
