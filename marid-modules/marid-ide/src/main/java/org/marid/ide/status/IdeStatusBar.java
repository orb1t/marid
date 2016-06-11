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

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import org.controlsfx.control.StatusBar;
import org.marid.ee.SingletonScoped;

import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@SingletonScoped
public class IdeStatusBar extends StatusBar {

    @Inject
    public IdeStatusBar(IdeStatusTimer ideStatusTimer, IdeStatusProfile ideStatusProfile) {
        setText("Marid Version 0.8");
        getRightItems().addAll(separator(), ideStatusProfile, separator(), ideStatusTimer);
    }

    private Separator separator() {
        final Separator separator = new Separator(Orientation.VERTICAL);
        separator.setMinWidth(10.0);
        return separator;
    }
}
