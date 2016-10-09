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

package org.marid.ide.tabs;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeKeyTab extends IdeTab {

    public IdeKeyTab(Node content, String text, Object... args) {
        super(content, text, args);
    }

    @Override
    protected void register() {
        final Tab tab = ideTabPane.getTabs().stream().filter(getClass()::isInstance).findAny().orElse(null);
        if (tab != null) {
            context.close();
            ideTabPane.getSelectionModel().select(tab);
        } else {
            super.register();
        }
    }
}
