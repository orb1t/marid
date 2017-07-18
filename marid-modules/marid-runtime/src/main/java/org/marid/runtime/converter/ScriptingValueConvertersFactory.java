package org.marid.runtime.converter;

import org.marid.runtime.context.MaridRuntime;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScriptingValueConvertersFactory implements ValueConvertersFactory {

    @Override
    public ValueConverters converters(MaridRuntime runtime) {
        return new ScriptingValueConverters(runtime);
    }
}
