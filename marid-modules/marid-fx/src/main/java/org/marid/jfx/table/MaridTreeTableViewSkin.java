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

package org.marid.jfx.table;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTreeTableViewSkin<S> extends TreeTableViewSkin<S> {

    public MaridTreeTableViewSkin(TreeTableView<S> treeTableView) {
        super(treeTableView);
    }

    @Override
    public void resizeColumnToFitContent(TreeTableColumn<S, ?> tc, int maxRows) {
        super.resizeColumnToFitContent(tc, maxRows);
    }

    public void refresh() {
        rowCountDirty = true;
        if (getSkinnable() != null) {
            getSkinnable().requestLayout();
        }
    }
}
