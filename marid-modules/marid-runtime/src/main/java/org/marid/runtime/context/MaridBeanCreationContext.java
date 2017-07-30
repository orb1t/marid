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
import org.marid.misc.Casts;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.marid.runtime.converter.DefaultValueConvertersManager;
import org.marid.runtime.converter.ValueConverter;
import org.marid.runtime.exception.*;

import java.lang.invoke.MethodHandle;
import java.util.*;

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
    private final List<Throwable> throwables = new ArrayList<>();
    private final DefaultValueConvertersManager convertersManager;

    final MaridRuntimeObject runtime;

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
        final Bean bean = Suppliers.get(beanMap, name, MaridBeanNotFoundException::new);
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
        final Object factoryObject = !bean.factory.contains(".") ? getOrCreate(bean.factory) : null;
        final Class<?> factoryClass = !bean.factory.contains(".")
                ? beanClasses.get(bean.factory)
                : MaridRuntimeUtils.loadClass(context.classLoader, name, bean.factory);
        final MethodHandle constructor = bind(bean.producer, bean.findProducer(factoryClass), factoryObject);

        beanClasses.put(name, constructor.type().returnType());
        final Object instance;
        {
            final Class<?>[] argTypes = constructor.type().parameterArray();
            final Object[] args = new Object[argTypes.length];
            for (int i = 0; i < args.length; i++) {
                final BeanMethodArg arg = bean.producer.args[i];
                args[i] = arg(bean, bean.producer, arg, argTypes[i]);
            }
            instance = invoke(bean, bean.producer, constructor, args);
        }

        if (instance != null) {
            for (final BeanMethod initializer : bean.initializers) {
                final MethodHandle handle = bind(initializer, bean.findInitializer(constructor, initializer), instance);
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
            return handle.invokeWithArguments(args);
        } catch (Throwable x) {
            throw new MaridBeanMethodInvocationException(bean.name, method.name(), x);
        }
    }

    private Object arg(Bean bean, BeanMethod method, BeanMethodArg methodArg, Class<?> type) {
        try {
            final ValueConverter converter = convertersManager
                    .getConverter(methodArg.type)
                    .orElseThrow(() -> new MaridBeanArgConverterNotFoundException(bean, method, methodArg));
            final Object arg = converter.convert(runtime.resolvePlaceholders(methodArg.value), Casts.cast(type));
            return bean.filtered(method, methodArg, methodArg.filter, arg);
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
