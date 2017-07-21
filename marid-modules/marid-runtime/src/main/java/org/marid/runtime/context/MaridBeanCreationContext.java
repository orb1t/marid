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

import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethodArg;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.converter.DefaultValueConvertersManager;
import org.marid.runtime.exception.MaridCircularBeanException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridBeanCreationContext implements AutoCloseable {

    private final MaridContext context;
    private final Map<String, Bean> beanMap;
    private final Map<String, Class<?>> beanClasses = new HashMap<>();
    private final Set<String> creationBeanNames = new LinkedHashSet<>();
    private final AtomicReference<Supplier<String>> lastMessage = new AtomicReference<>(() -> null);
    private final List<Throwable> throwables = new ArrayList<>();
    private final DefaultValueConvertersManager convertersManager;
    private final MaridRuntimeObject runtime;

    MaridBeanCreationContext(MaridConfiguration configuration, MaridContext context) {
        this.context = context;
        this.beanMap = of(configuration.beans).collect(toMap(e -> e.name, identity()));
        this.runtime = new MaridRuntimeObject(context, this::getOrCreate);
        this.convertersManager = new DefaultValueConvertersManager(context.classLoader, runtime);
    }

    Object getOrCreate(String name) {
        try {
            return context.beans.computeIfAbsent(name, this::create);
        } catch (Throwable x) {
            throwables.add(x);
            throw x;
        }
    }

    private Object create(String name) {
        final Bean bean = requireNonNull(beanMap.get(name), () -> "No such bean " + name);
        if (creationBeanNames.add(name)) {
            try {
                return create0(name, bean);
            } catch (RuntimeException x) {
                throw x;
            } catch (Throwable x) {
                throw new IllegalStateException(x);
            } finally {
                creationBeanNames.remove(name);
            }
        } else {
            throw new MaridCircularBeanException(creationBeanNames, name);
        }
    }

    private Object create0(String name, Bean bean) throws Throwable {
        lastMessage.set(() -> String.format("[%s] factory setup: %s", name, bean.factory));
        final Object factoryObject;
        final Class<?> factoryClass;
        if (Bean.type(bean.factory) != null) {
            factoryClass = context.classLoader.loadClass(Bean.type(bean.factory));
            factoryObject = null;
        } else {
            factoryObject = getOrCreate(Bean.ref(bean.factory));
            factoryClass = beanClasses.get(Bean.ref(bean.factory));
        }
        lastMessage.set(() -> String.format("[%s] producer lookup: %s", name, bean.producer));
        final MethodHandle constructor = bind(bean.producer, bean.findProducer(factoryClass), factoryObject);

        beanClasses.put(name, constructor.type().returnType());
        final Object instance;
        {
            final Class<?>[] argTypes = constructor.type().parameterArray();
            lastMessage.set(() -> String.format("[%s] arguments setup", name));
            final Object[] args = new Object[argTypes.length];
            for (int i = 0; i < args.length; i++) {
                final BeanMethodArg arg = bean.producer.args[i];
                lastMessage.set(() -> String.format("[%s] argument setup: %s", name, arg));
                args[i] = arg(arg, argTypes[i]);
            }
            lastMessage.set(() -> String.format("[%s] constructor invocation", name));
            instance = constructor.invokeWithArguments(args);
        }

        if (instance != null) {
            lastMessage.set(() -> String.format("[%s] initializers", name));
            final MethodHandle[] initializers = Bean.findInitializers(constructor, bean.initializers);
            for (int k = 0; k < initializers.length; k++) {
                final BeanMethod initializer = bean.initializers[k];
                initializers[k] = bind(initializer, initializers[k], instance);
                lastMessage.set(() -> String.format("%s initializer %s", name, initializer));
                final Class<?>[] argTypes = initializers[k].type().parameterArray();
                lastMessage.set(() -> String.format("[%s] arguments setup: %s", name, initializer));
                final Object[] args = new Object[argTypes.length];
                for (int i = 0; i < args.length; i++) {
                    final BeanMethodArg arg = initializer.args[i];
                    lastMessage.set(() -> String.format("[%s] argument setup: %s", name, arg));
                    args[i] = arg(arg, argTypes[i]);
                }
                lastMessage.set(() -> String.format("[%s] initializer invocation: %s", name, initializer));
                initializers[k].invokeWithArguments(args);
            }
            context.initialize(name, instance);
        }
        return instance;
    }

    private Object arg(BeanMethodArg member, Class<?> type) throws Throwable {
        final Object arg;
        if (member.value == null) {
            arg = MaridRuntimeUtils.defaultValue(type);
        } else {
            arg = convertersManager.getConverter(member.type)
                    .map(c -> c.apply(runtime.resolvePlaceholders(member.value)))
                    .orElseThrow(() -> new IllegalArgumentException("Unable to find converter for " + member.type));
        }
        if (arg == null || member.filter == null) {
            return arg;
        }
        final MethodHandle argHandle = MethodHandles.constant(arg.getClass(), arg);
        return Bean.filtered(member.filter, argHandle).invoke();
    }

    private MethodHandle bind(BeanMethod producer, MethodHandle handle, Object instance) {
        if (handle.type().parameterCount() == producer.args.length + 1) {
            return handle.bindTo(instance);
        } else {
            return handle;
        }
    }

    @Override
    public void close() {
        if (!throwables.isEmpty()) {
            try {
                context.close();
            } catch (Throwable x) {
                throwables.add(x);
            }
            throw new MaridContextException(lastMessage.get().get(), throwables);
        }
    }
}
