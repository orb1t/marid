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
package org.marid.ide

import org.codehaus.groovy.control.CompilerConfiguration
import org.marid.ide.menu.MaridMenu
import org.marid.ide.util.IdeUncaughtExceptionHandler
import org.marid.logging.Logging

Logging.init(getClass(), "logide.properties");
Thread.defaultUncaughtExceptionHandler = new IdeUncaughtExceptionHandler();

def ccl = Thread.currentThread().contextClassLoader;
def cc = new CompilerConfiguration();
cc.sourceEncoding = "UTF-8";
cc.recompileGroovySource = true;
cc.targetBytecode = "1.7";
def sl = ServiceLoader.load(MaridMenu, new GroovyClassLoader(ccl, cc, false));
for (def menu in sl) {
    println(menu.menuEntries);
}