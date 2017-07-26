/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.jfx.control;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.lang.ref.Cleaner;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridControls {

    static Cleaner FXCLEANER = Cleaner.create();

    static ScrollPane createMaridScrollPane(Node node) {
        final ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent");
        return scrollPane;
    }
}
