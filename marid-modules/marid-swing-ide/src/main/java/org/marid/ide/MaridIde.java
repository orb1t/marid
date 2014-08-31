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

import groovy.lang.Script;
import org.marid.groovy.GroovyRuntime;
import org.marid.logging.LogSupport;
import org.marid.logging.Logging;
import org.marid.swing.SwingUtil;
import org.marid.swing.log.SwingHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.logging.Logger;

import static org.marid.groovy.GroovyRuntime.CLASS_LOADER;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde implements LogSupport {

    public static void main(String[] args) throws Exception {
        Logging.init("marid-ide-logging.properties");
        Logger.getLogger("").addHandler(new SwingHandler());
        Thread.setDefaultUncaughtExceptionHandler((t, x) -> Log.warning("Uncaught exception in {0}", x, t));
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setClassLoader(CLASS_LOADER);
        context.addApplicationListener(event -> Log.info("{0}", event));
        for (final Enumeration<URL> e = CLASS_LOADER.getResources("context/init.groovy"); e.hasMoreElements(); ) {
            try (final Reader reader = new InputStreamReader(e.nextElement().openStream(), StandardCharsets.UTF_8)) {
                final Script script = GroovyRuntime.SHELL.parse(reader);
                script.getBinding().setVariable("context", context);
                script.run();
            }
        }
        context.refresh();
        SwingUtil.execute(context::start);
    }
}
