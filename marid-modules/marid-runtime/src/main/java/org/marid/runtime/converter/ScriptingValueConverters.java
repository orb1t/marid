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

    private final Map<String, MetaLiteral> info = new HashMap<>();
    private final Map<String, Function<String, ?>> functions = new HashMap<>();

    public ScriptingValueConverters(MaridRuntime runtime) {
        final ScriptEngineManager manager = new ScriptEngineManager(runtime.classLoader);
        manager.getEngineFactories().forEach(scriptEngineFactory -> {
            final String description = String.format("%s(%s) %s(%s)",
                    scriptEngineFactory.getEngineName(),
                    scriptEngineFactory.getEngineVersion(),
                    scriptEngineFactory.getLanguageName(),
                    scriptEngineFactory.getLanguageVersion()
            );
            final ScriptEngine engine = scriptEngineFactory.getScriptEngine();
            engine.put("bean", runtime.beanFunc);
            engine.put("runtime", runtime);
            final Function<String, ?> function = script -> {
                try {
                    return engine.eval(script);
                } catch (ScriptException se) {
                    throw new IllegalStateException(se);
                }
            };
            scriptEngineFactory.getExtensions().forEach(ext -> {
                info.put(ext, new MetaLiteral(ext, "D_SCRIPT", description));
                functions.put(ext, function);
            });
        });
    }

    @Override
    public Map<Type, Map<String, MetaLiteral>> getConverters() {
        return Collections.singletonMap(Object.class, info);
    }

    @Override
    public Function<String, ?> getConverter(String name) {
        return functions.get(name);
    }
}
