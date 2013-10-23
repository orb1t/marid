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

package org.marid;

import org.marid.groovy.GroovyRuntime;
import org.marid.logging.Logging;
import org.marid.service.MaridServices;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Marid implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(Marid.class.getName());
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public static void main(String... args) {
        TimeZone.setDefault(UTC);
        Logging.init(Marid.class, "log.properties");
        Thread.setDefaultUncaughtExceptionHandler(new Marid());
        Thread.currentThread().setContextClassLoader(GroovyRuntime.CLASS_LOADER);
        MaridServices.start();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        warning(LOG, "Uncaught exception in {0}", e, t);
    }
}