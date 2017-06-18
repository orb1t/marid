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

package org.marid.ide.configurations;

import org.marid.Ide;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class WindowConfiguration {

    @IdeAction
    public FxAction alwaysOnTopAction() {
        return new FxAction("ops", "Window")
                .bindText("Always on top")
                .setIcon("D_BORDER_TOP")
                .bindSelected(Ide.primaryStage.alwaysOnTopProperty())
                .setEventHandler(event -> Ide.primaryStage.setAlwaysOnTop(!Ide.primaryStage.isAlwaysOnTop()));
    }
}
