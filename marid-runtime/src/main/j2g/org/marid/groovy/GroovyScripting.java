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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.marid.Scripting;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import static org.marid.groovy.MaridGroovyMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyScripting extends Scripting {

    private static final Logger LOG = Logger.getLogger(GroovyScripting.class.getName());

    private CompilerConfiguration getCompilerConfiguration() {
        try (InputStream is = getClass().getResourceAsStream("/groovy.properties")) {
            if (is != null) {
                Properties properties = new Properties();
                properties.load(is);
                return new CompilerConfiguration(properties);
            }
        } catch (IOException x) {
            warning(LOG, "Unable to load compiler configuration", x);
        }
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setSourceEncoding("UTF-8");
        cc.setTargetBytecode("1.7");
        cc.setRecompileGroovySource(true);
        cc.setDebug(true);
        cc.setVerbose(false);
        return cc;
    }

    private GroovyClassLoader getClassLoader() {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        if (currentLoader == null) {
            currentLoader = getClass().getClassLoader();
        }
        return new GroovyClassLoader(currentLoader, getCompilerConfiguration());
    }

    @Override
    public Bundle getBundle() throws Exception {
        final GroovyClassLoader classLoader = getClassLoader();
        GroovyScriptEngineFactory engineFactory = new GroovyScriptEngineFactory() {
            @Override
            public ScriptEngine getScriptEngine() {
                return new GroovyScriptEngineImpl(classLoader);
            }
        };
        for (String extension : engineFactory.getExtensions()) {
            MANAGER.registerEngineExtension(extension, engineFactory);
        }
        for (String mime : engineFactory.getMimeTypes()) {
            MANAGER.registerEngineMimeType(mime, engineFactory);
        }
        for (String name : engineFactory.getNames()) {
            MANAGER.registerEngineName(name, engineFactory);
        }
        return new Bundle(MANAGER.getEngineByExtension("groovy"), classLoader);
    }
}
