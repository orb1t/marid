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

import groovy.lang.GroovyCodeSource;
import org.marid.Versioning;
import org.marid.groovy.GroovyRuntime;
import org.marid.ide.swing.SwingIde;
import org.marid.logging.Logging;
import org.marid.swing.log.SwingHandler;

import javax.swing.*;
import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());

    public static void main(String[] args) throws Exception {
        Logging.init(MaridIde.class, "marid-ide-logging.properties");
        Logger.getLogger("").addHandler(new SwingHandler());
        Thread.setDefaultUncaughtExceptionHandler(new MaridIde());
        Thread.currentThread().setContextClassLoader(GroovyRuntime.CLASS_LOADER);
        info(LOG, "Starting Marid {0} on {1} {2}",
                Versioning.getImplementationVersion(MaridIde.class),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"));
        final URL url = Thread.currentThread().getContextClassLoader().getResource("Init.groovy");
        if (url != null) {
            try {
                GroovyRuntime.SHELL.evaluate(new GroovyCodeSource(url));
            } catch (Exception x) {
                JOptionPane.showMessageDialog(null, x, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            EventQueue.invokeLater(SwingIde::run);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        warning(LOG, "Uncaught exception in {0}", e, t);
    }
}
