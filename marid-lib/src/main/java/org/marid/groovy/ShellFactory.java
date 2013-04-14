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

package org.marid.groovy;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class ShellFactory {

    private static GroovyShell sh;
    private static ServiceLoader<ShellSupplier> loader;

    public CompilerConfiguration getDefaultCompilerConfiguration() {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setRecompileGroovySource(false);
        cc.setTargetBytecode("1.7");
        cc.setSourceEncoding("UTF-8");
        return cc;
    }

    public synchronized GroovyShell getShell() {
        if (loader == null) {
            loader = ServiceLoader.load(ShellSupplier.class);
        }
        Iterator<ShellSupplier> it = loader.iterator();
        if (it.hasNext()) {
            return it.next().getShell();
        } else {
            return sh == null ? sh = new GroovyShell(getDefaultCompilerConfiguration()) : sh;
        }
    }
}
