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

import groovy.util.logging.Log
import org.marid.ide.itf.Application
import org.marid.ide.splash.MaridSplash
import org.marid.ide.splash.MaridSplashHandler
import org.marid.ide.swing.impl.ApplicationImpl

import javax.swing.*
import java.awt.*
import java.awt.event.ActionListener
import java.util.logging.Logger

/**
 * IDE class.
 *
 * @author Dmitry Ovchinnikov 
 */
@Log
class Ide {

    public static final Application APPLICATION;

    static {
        try {
            APPLICATION = new ApplicationImpl();
        } catch (x) {
            log.severe("Application error", x)
        }
    }

    static void init(final MaridSplash splash) {
        if (splash.supported) {
            final def rootLogger = Logger.global.parent;
            if (rootLogger != null) {
                final def splashHandler = new MaridSplashHandler(splash);
                rootLogger.addHandler(splashHandler);
                EventQueue.invokeLater {
                    new Timer(1000, {
                        if (!splash.visible) {
                            rootLogger.removeHandler(splashHandler);
                            def timer = (Timer)it.source;
                            timer.stop();
                        }
                    } as ActionListener).start();
                }
            }
        }
    }
}
