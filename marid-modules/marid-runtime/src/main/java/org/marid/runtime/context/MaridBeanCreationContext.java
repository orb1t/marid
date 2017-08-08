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

import org.marid.misc.Casts;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.marid.runtime.converter.DefaultValueConvertersManager;
import org.marid.runtime.converter.ValueConverter;
import org.marid.runtime.exception.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static org.marid.runtime.context.MaridRuntimeUtils.*;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridBeanCreationContext implements AutoCloseable {

    private final MaridConfiguration configuration;
    private final ClassLoader classLoader;
    private final MaridContext context;
    private final Set<String> creationBeanNames = new LinkedHashSet<>();
    private final Set<Throwable> throwables = new LinkedHashSet<>();
    private final DefaultValueConvertersManager convertersManager;

    final MaridRuntimeObject runtime;

    MaridBeanCreationContext(MaridConfiguration configuration, ClassLoader classLoader, MaridContext context) {
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.context = context;
        this.runtime = new MaridRuntimeObject(context, classLoader, this::getOrCreate);
        this.convertersManager = new DefaultValueConvertersManager(classLoader, runtime);
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
        final Bean bean = Stream.of(configuration.beans)
                .filter(b -> Objects.equals(name, b.name))
                .findFirst()
                .orElseThrow(() -> new MaridBeanNotFoundException(name));
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

    private Object create0(String name, Bean bean) {
        final Member factory = fromSignature(bean.signature, classLoader);
        final Object factoryObject = isRoot(factory) ? null : getOrCreate(bean.factory);
        final MethodHandle constructor = bind(bean, producer(factory), factoryObject);

        final Object instance;
        {
            final Class<?>[] argTypes = constructor.type().parameterArray();
            final Object[] args = new Object[argTypes.length];
            for (int i = 0; i < args.length; i++) {
                final BeanMethodArg arg = bean.args[i];
                args[i] = arg(bean, bean, arg, argTypes[i]);
            }
            instance = invoke(bean, bean, constructor, args);
        }

        if (instance != null) {
            for (final BeanMethod initializer : bean.initializers) {
                final Member member = fromSignature(initializer.signature, classLoader);
                final MethodHandle handle = bind(initializer, initializer(member), instance);
                final Class<?>[] argTypes = handle.type().parameterArray();
                final Object[] args = new Object[argTypes.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = arg(bean, initializer, initializer.args[i], argTypes[i]);
                }
                invoke(bean, initializer, handle, args);
            }
            context.initialize(name, instance);
        }
        return instance;
    }

    private Object invoke(Bean bean, BeanMethod method, MethodHandle handle, Object... args) {
        try {
            return handle.asFixedArity().invokeWithArguments(args);
        } catch (Throwable x) {
            throw new MaridBeanMethodInvocationException(bean.name, method.name(), x);
        }
    }

    private Object arg(Bean bean, BeanMethod method, BeanMethodArg methodArg, Class<?> type) {
        try {
            final ValueConverter converter = convertersManager
                    .getConverter(methodArg.type)
                    .orElseThrow(() -> new MaridBeanArgConverterNotFoundException(bean, method, methodArg));
            return converter.convert(runtime.resolvePlaceholders(methodArg.value), Casts.cast(type));
        } catch (RuntimeException x) {
            throw x;
        } catch (Throwable x) {
            throw new MaridBeanMethodArgException(bean.name, method.name(), methodArg.name, x);
        }
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
            throw new MaridContextException("Context initialization failed", throwables);
        }
    }
}
