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
import java.util.Optional;

import static org.marid.runtime.context.MaridRuntimeUtils.*;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridCreationContext implements AutoCloseable {

    private final MaridCreationContext parent;
    private final Bean bean;
    private final MaridContext context;
    private final DefaultValueConvertersManager convertersManager;
    private final LinkedHashSet<String> processing = new LinkedHashSet<>();

    MaridCreationContext(MaridCreationContext parent, Bean bean, MaridContext context) {
        this.parent = parent;
        this.bean = bean;
        this.context = context;
        this.convertersManager = new DefaultValueConvertersManager(context);
    }

    private Object getOrCreate(String name) {
        try {
            return context.getBean(name);
        } catch (MaridBeanNotFoundException x) {
            return create(name);
        }
    }

    private Object create(String name) {
        try {
            final Optional<Bean> b = bean.children.stream().filter(e -> e.name.equals(name)).findFirst();
            if (b.isPresent() && processing.add(name)) {
                try {
                    final Object v = create(b.get());
                    context.children.add(new MaridContext(context.configuration, context, this, b.get(), v));
                    return v;
                } finally {
                    processing.remove(name);
                }
            }
        } catch (RuntimeException x) {
            throw x;
        } catch (Throwable x) {
            throw new MaridBeanInitializationException(name, x);
        }
        if (parent != null) {
            return parent.create(name);
        } else {
            throw new MaridBeanNotFoundException(name);
        }
    }

    Object create(Bean bean) {
        final Member factory = fromSignature(bean.signature, context.getClassLoader());
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
                final Member member = fromSignature(initializer.signature, context.getClassLoader());
                final MethodHandle handle = bind(initializer, initializer(member), instance);
                final Class<?>[] argTypes = handle.type().parameterArray();
                final Object[] args = new Object[argTypes.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = arg(bean, initializer, initializer.args[i], argTypes[i]);
                }
                invoke(bean, initializer, handle, args);
            }
            context.initialize(bean.name, instance);
        }

        return instance;
    }

    private Object invoke(Bean bean, BeanMethod method, MethodHandle handle, Object... args) {
        try {
            return handle.asFixedArity().invokeWithArguments(args);
        } catch (Throwable x) {
            throw new MaridBeanMethodInvocationException(bean.name, method.signature, x);
        }
    }

    private Object arg(Bean bean, BeanMethod method, BeanMethodArg methodArg, Class<?> type) {
        try {
            switch (methodArg.type) {
                case "ref":
                    return getOrCreate(methodArg.value);
                default: {
                    final ValueConverter converter = convertersManager
                            .getConverter(methodArg.type)
                            .orElseThrow(() -> new MaridBeanArgConverterNotFoundException(bean, method, methodArg));
                    return converter.convert(context.resolvePlaceholders(methodArg.value), Casts.cast(type));
                }
            }
        } catch (RuntimeException x) {
            throw x;
        } catch (Throwable x) {
            throw new MaridBeanMethodArgException(bean.name, method.signature, methodArg.name, x);
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
        processing.clear();
    }
}
