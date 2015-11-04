/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.ide.gui;

import org.marid.ide.MaridIde;
import org.marid.ide.base.Ide;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("ide")
public class IdeImpl implements Ide, SysPrefSupport, LogSupport {

    @Override
    public void exit() {
        MaridIde.CONTEXT.close();
        System.exit(0);
    }

    @Override
    public String getName() {
        return "ide";
    }
}
