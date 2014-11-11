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
import org.marid.bd.Block;
import org.marid.groovy.GroovyRuntime;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.swing.context.GuiContext;
import org.marid.ide.swing.gui.IdeImpl;
import org.marid.ide.widgets.Widget;
import org.marid.logging.LogSupport;
import org.marid.swing.SwingUtil;
import org.marid.util.MaridInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import static org.marid.groovy.GroovyRuntime.CLASS_LOADER;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde implements LogSupport {

    public static void main(String[] args) throws Exception {
        MaridInitializer.visitInitializers(MaridInitializer::init);
        Thread.setDefaultUncaughtExceptionHandler((t, x) -> Log.warning("Uncaught exception in {0}", x, t));
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setClassLoader(CLASS_LOADER);
        context.addApplicationListener(event -> Log.info("{0}", event));
        context.scan(
                GuiContext.class.getPackage().getName(),
                IdeImpl.class.getPackage().getName(),
                ProfileManager.class.getPackage().getName(),
                Block.class.getPackage().getName(),
                Widget.class.getPackage().getName(),
                MaridFrame.class.getPackage().getName());
        init(context);
        context.refresh();
        SwingUtil.execute(context::start);
    }

    private static void init(AnnotationConfigApplicationContext context) throws Exception {
        for (final Enumeration<URL> e = CLASS_LOADER.getResources("context/init.groovy"); e.hasMoreElements(); ) {
            final URL url = e.nextElement();
            Log.info("Executing {0}", url);
            try (final Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                final Script script = GroovyRuntime.SHELL.parse(reader);
                script.getBinding().setVariable("context", context);
                script.run();
            } catch (Exception x) {
                Log.warning("Unable to execute {0}", x, url);
            }
        }
    }
}
