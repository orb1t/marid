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

package org.marid.runtime.context;

import org.marid.function.Suppliers;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanConstructor;
import org.marid.runtime.beans.BeanMember;
import org.marid.runtime.beans.BeanProperties;
import org.marid.runtime.converter.ValueConverters;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridBeanCreationContext implements AutoCloseable {

    private static final Pattern COMMA = Pattern.compile(",");

    private final ClassLoader classLoader;
    private final MaridRuntime runtime;
    private final Supplier<ScriptEngineManager> managerRef;
    private final Map<String, ScriptEngine> engines = new HashMap<>();
    private final Map<String, Bean> beanMap;
    private final Map<Bean, Entry<String, String[]>> producerMap = new IdentityHashMap<>();
    private final IllegalStateException startException = new IllegalStateException("Runtime start exception");
    private final ServiceLoader<ValueConverters> valueConverters;

    MaridBeanCreationContext(MaridContext context, MaridRuntime runtime, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.runtime = runtime;
        this.managerRef = Suppliers.memoized(() -> {
            final ScriptEngineManager manager = new ScriptEngineManager(classLoader);
            manager.put("runtime", new MaridObject(runtime.beans::get, runtime::isActive));
            return manager;
        });
        this.beanMap = of(context.beans).collect(toMap(e -> e.name, identity()));
        this.valueConverters = ServiceLoader.load(ValueConverters.class, classLoader);
    }

    private ScriptEngine getEngine(String name) {
        return engines.computeIfAbsent(name, managerRef.get()::getEngineByExtension);
    }

    Object getOrCreate(String name) {
        return runtime.beans.computeIfAbsent(name, this::create);
    }

    private Object create(String name) {
        final Bean bean = requireNonNull(beanMap.get(name), () -> "No such bean " + name);
        final Entry<Object, Class<?>> f = factoryEntry(bean);
        final Entry<String, String[]> p = producerMap.computeIfAbsent(bean, this::producerEntry);

        final BeanConstructor constructor = bean.findProducer(p, f.getValue(), f.getKey());
        final Object[] args = new Object[bean.args.length];
        final Class<?>[] argClasses = constructor.handle.type().parameterArray();
        for (int i = 0; i < args.length; i++) {
            final BeanMember arg = bean.args[i];
            if (arg.value == null) {
                args[i] = MaridRuntimeUtils.defaultValue(argClasses[i]);
            } else {
                args[i] = arg(arg, constructor.args[i]);
            }
        }

        try {
            final Object instance = constructor.handle.invokeWithArguments(args);
            if (instance != null) {
                final BeanProperties properties = bean.findProperties(constructor);
                for (int i = 0; i < bean.props.length; i++) {
                    final Type type = properties.types[i];
                    final MethodHandle setter = properties.setters[i].bindTo(instance);
                    final Object value = arg(bean.props[i], type);
                    try {
                        setter.invokeWithArguments(value);
                    } catch (Throwable x) {
                        throw new IllegalArgumentException("Unable to apply " + value + " to " + setter, x);
                    }
                }
                runtime.initialize(name, instance);
            }
            return instance;
        } catch (RuntimeException x) {
            throw x;
        } catch (Throwable x) {
            throw new IllegalStateException(x);
        }
    }

    private Object arg(BeanMember member, Type type) {
        switch (member.type) {
            case "ref": return getOrCreate(member.value);
            case "val": return stream(valueConverters.spliterator(), false)
                    .map(c -> c.getConverter(type))
                    .filter(Objects::nonNull)
                    .map(c -> c.apply(member.value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No converter for " + member + "(" + type + ")"));
            default: {
                final String e = member.type;
                final ScriptEngine engine = requireNonNull(getEngine(e), () -> "Unknown engine: " + e);
                try {
                    return engine.eval(member.value);
                } catch (ScriptException x) {
                    throw new IllegalArgumentException("Script error for " + member, x);
                }
            }
        }
    }

    private Entry<String, String[]> producerEntry(Bean b) {
        final int index = b.producer.indexOf('(');
        if (index < 0) {
            return new SimpleImmutableEntry<>(b.producer, new String[0]);
        } else {
            final String producerName = b.producer.substring(0, index);
            final String[] args = COMMA.splitAsStream(b.producer.substring(index + 1, b.producer.length() - 1))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .toArray(String[]::new);
            return new SimpleImmutableEntry<>(producerName, args);
        }
    }

    private Entry<Object, Class<?>> factoryEntry(Bean bean) {
        if (bean.factory.contains(".")) {
            try {
                if (bean.factory.endsWith(".class")) {
                    final String className = bean.factory.substring(0, bean.factory.length() - 6);
                    final Class<?> type = Class.forName(className, true, classLoader);
                    return new SimpleImmutableEntry<>(type, type.getClass());
                } else {
                    final Class<?> type = Class.forName(bean.factory, true, classLoader);
                    return new SimpleImmutableEntry<>(null, type);
                }
            } catch (ClassNotFoundException x) {
                throw new IllegalStateException(x);
            }
        } else {
            final Object instance = getOrCreate(bean.factory);
            return new SimpleImmutableEntry<>(instance, instance.getClass());
        }
    }

    @Override
    public void close() {
        if (startException.getSuppressed().length > 0) {
            try {
                runtime.close();
            } catch (Throwable x) {
                startException.addSuppressed(x);
            }
            throw startException;
        }
    }
}
