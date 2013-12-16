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

import org.marid.ide.itf.Application;
import org.marid.ide.itf.ApplicationFactory;
import org.marid.ide.swing.impl.ApplicationFactoryImpl;

import java.lang.invoke.MethodHandles;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    public static final Application APPLICATION;

    static {
        ApplicationFactory factory = null;
        try {
            for (final ApplicationFactory applicationFactory : ServiceLoader.load(ApplicationFactory.class)) {
                factory = applicationFactory;
            }
        } catch (Exception x) {
            warning(LOG, "Unable to create an application instance", x);
        } finally {
            if (factory == null) {
                factory = new ApplicationFactoryImpl();
            }
        }
        APPLICATION = factory.createApplication();
    }
}
