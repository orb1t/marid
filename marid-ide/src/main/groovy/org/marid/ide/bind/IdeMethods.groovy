/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.bind

import org.marid.ide.Ide
import org.marid.ide.itf.Application
import org.marid.ide.menu.MenuSupport

/**
 * IDE methods.
 *
 * @author Dmitry Ovchinnikov
 */
class IdeMethods {

    public static Application getApplication(Object script) {
        return Ide.application;
    }

    public static Application getApplication(MenuSupport menu) {
        return Ide.application;
    }
}
