package org.marid.runtime.converter;

import org.marid.runtime.context.MaridRuntime;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.ServiceLoader.load;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultValueConvertersManager {

    protected final ValueConverters[] valueConverters;

    public DefaultValueConvertersManager(ClassLoader classLoader, MaridRuntime runtime) {
        valueConverters = StreamSupport.stream(load(ValueConvertersFactory.class, classLoader).spliterator(), false)
                .map(c -> c.converters(runtime))
                .toArray(ValueConverters[]::new);
    }

    public Function<String, ?> getConverter(String name) {
        return Stream.of(valueConverters)
                .map(c -> c.getConverter(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No converters found for name: " + name));
    }
}
