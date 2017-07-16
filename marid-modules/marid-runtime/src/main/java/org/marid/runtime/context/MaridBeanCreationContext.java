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

import org.marid.runtime.beans.BeanConstructor;
import org.marid.runtime.beans.BeanInfo;
import org.marid.runtime.beans.BeanMember;
import org.marid.runtime.beans.BeanProperties;
import org.marid.runtime.converter.ValueConverters;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
class MaridBeanCreationContext implements AutoCloseable {

    private final ClassLoader classLoader;
    private final MaridRuntime runtime;
    private final ScriptEngineManager manager;
    private final Map<String, ScriptEngine> engines = new HashMap<>();
    private final Map<String, BeanInfo> beanInfoMap;
    private final IllegalStateException startException = new IllegalStateException("Runtime start exception");
    private final ServiceLoader<ValueConverters> valueConverters;

    MaridBeanCreationContext(MaridContext context, MaridRuntime runtime, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.runtime = runtime;
        this.manager = new ScriptEngineManager(classLoader);
        this.manager.put("runtime", new MaridObject(runtime.beans::get, runtime::isActive));
        this.beanInfoMap = of(context.beans).collect(toMap(e -> e.name, identity()));
        this.valueConverters = ServiceLoader.load(ValueConverters.class, classLoader);
    }

    private ScriptEngine getEngine(String name) {
        return engines.computeIfAbsent(name, manager::getEngineByExtension);
    }

    Object getOrCreate(String name) {
        return runtime.beans.computeIfAbsent(name, this::create);
    }

    private Object create(String name) {
        final BeanInfo beanInfo = requireNonNull(beanInfoMap.get(name), () -> "No such bean " + name);
        final Entry<Object, Class<?>> f = factoryEntry(beanInfo);

        final BeanConstructor constructor = beanInfo.findProducer(beanInfo.producer, f.getValue(), f.getKey());
        final Object[] args = new Object[beanInfo.args.length];
        final Class<?>[] argClasses = constructor.handle.type().parameterArray();
        for (int i = 0; i < args.length; i++) {
            final BeanMember arg = beanInfo.args[i];
            if (arg.value == null) {
                args[i] = MaridRuntimeUtils.defaultValue(argClasses[i]);
            } else {
                args[i] = arg(arg, constructor.args[i]);
            }
        }

        try {
            final Object instance = constructor.handle.invokeWithArguments(args);
            if (instance != null) {
                final BeanProperties properties = beanInfo.findProperties(constructor);
                for (int i = 0; i < beanInfo.props.length; i++) {
                    final Type type = properties.types[i];
                    final MethodHandle setter = properties.setters[i].bindTo(instance);
                    final Object value = arg(beanInfo.props[i], type);
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
            case "val": return StreamSupport.stream(valueConverters.spliterator(), false)
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

    private Entry<Object, Class<?>> factoryEntry(BeanInfo beanInfo) {
        switch (beanInfo.factory.type) {
            case "bean": {
                final Object instance = getOrCreate(beanInfo.factory.value);
                return new SimpleImmutableEntry<>(instance, instance.getClass());
            }
            case "class": {
                try {
                    final Class<?> type = Class.forName(beanInfo.factory.value, true, classLoader);
                    return new SimpleImmutableEntry<>(null, type);
                } catch (ClassNotFoundException x) {
                    throw new IllegalStateException(x);
                }
            }
            default:
                try {
                    final String e = beanInfo.factory.type;
                    final ScriptEngine engine = requireNonNull(getEngine(e), () -> "Unknown engine: " + e);
                    final Object instance = engine.eval(beanInfo.factory.value);
                    return new SimpleImmutableEntry<>(instance, instance.getClass());
                } catch (ScriptException x) {
                    throw new IllegalArgumentException(beanInfo.factory.toString(), x);
                }
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
