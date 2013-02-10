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

import org.marid.groovy.DslLoader
import org.marid.ide.gui.util.ImageGenDialog

import java.awt.*
import java.util.logging.LogManager

def classLoader = Thread.currentThread().getContextClassLoader();
def logConfiguration = classLoader.getResource("logide.properties");
if (logConfiguration != null) {
    logConfiguration.withInputStream {stream ->
        LogManager.logManager.readConfiguration(stream);
    }
}

DslLoader.loadDsl();

EventQueue.invokeLater {
    def dialog = new ImageGenDialog((Frame)null, "My dialog");
    dialog.visible = true;
}