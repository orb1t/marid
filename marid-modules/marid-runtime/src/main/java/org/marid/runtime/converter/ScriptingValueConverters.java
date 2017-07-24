/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.converter;

import org.marid.annotation.MetaLiteral;
import org.marid.runtime.context.MaridRuntime;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScriptingValueConverters implements ValueConverters {

    private final HashMap<String, MetaLiteral> info = new HashMap<>();
    private final HashMap<String, ValueConverter> functions = new HashMap<>();

    public ScriptingValueConverters(MaridRuntime runtime) {
        final ScriptEngineManager manager = new ScriptEngineManager(runtime.getClassLoader());
        manager.getEngineFactories().forEach(scriptEngineFactory -> {
            final String description = String.format("%s(%s) %s(%s)",
                    scriptEngineFactory.getEngineName(),
                    scriptEngineFactory.getEngineVersion(),
                    scriptEngineFactory.getLanguageName(),
                    scriptEngineFactory.getLanguageVersion()
            );
            final ScriptEngine engine = scriptEngineFactory.getScriptEngine();
            engine.put("bean", (Function<String, Object>) runtime::getBean);
            engine.put("runtime", runtime);
            final ValueConverter converter = (script, c) -> {
                try {
                    return c.cast(engine.eval(script));
                } catch (ScriptException se) {
                    throw new IllegalStateException(se);
                }
            };
            scriptEngineFactory.getExtensions().forEach(ext -> {
                info.put(ext, new MetaLiteral("Scripting", ext, "D_SCRIPT", description));
                functions.put(ext, converter);
            });
        });
    }

    @Override
    public ValueConverter getConverter(String name) {
        return functions.get(name);
    }

    @Override
    public Map<String, MetaLiteral> getMetaMap() {
        return info;
    }

    @Override
    public Map<String, Type> getTypeMap() {
        return Collections.emptyMap();
    }
}
