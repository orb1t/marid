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

package org.marid.ide;

import org.codehaus.groovy.runtime.m12n.ExtensionModuleRegistry;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestMaridIde {

    public static void main(String... args) throws Exception {
        File f = new File(new File(System.getProperty("user.dir")), "marid-ide/src/ext");
        URLClassLoader cl = new URLClassLoader(new URL[] {f.toURI().toURL()});
        Thread.currentThread().setContextClassLoader(cl);
        MaridIde.main(args);
        ExtensionModuleScanner sc;
    }
}
