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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static org.marid.groovy.MaridGroovyMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class Scripting {

    private static final Logger LOG = Logger.getLogger(Scripting.class.getName());
    public static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    public static final ScriptEngine ENGINE;
    public static final ClassLoader LOADER;

    static {
        ScriptEngine engine = null;
        ClassLoader classLoader = null;
        try {
            Iterator<Scripting> it = ServiceLoader.load(Scripting.class).iterator();
            if (it.hasNext()) {
                Bundle bundle = it.next().getBundle();
                engine = bundle.engine;
                classLoader = bundle.classLoader;
            }
        } catch (Exception x) {
            severe(LOG, "Unable to load scripting", x);
            System.exit(1);
        }
        if (engine == null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            Iterator<ScriptEngineFactory> it = manager.getEngineFactories().iterator();
            if (it.hasNext()) {
                engine = it.next().getScriptEngine();
            } else {
                severe(LOG, "Cannot find a scripting engine");
                System.exit(2);
            }
        }
        ENGINE = engine;
        LOADER = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    public abstract Bundle getBundle() throws Exception;

    protected class Bundle {

        protected final ScriptEngine engine;
        protected final ClassLoader classLoader;

        public Bundle(ScriptEngine engine, ClassLoader classLoader) {
            this.engine = engine;
            this.classLoader = classLoader;
        }
    }
}
