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
import org.marid.runtime.beans.BeanFactory;
import org.marid.runtime.beans.BeanMember;
import org.marid.runtime.converter.DefaultValueConvertersManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridBeanCreationContext implements AutoCloseable {

    private final ClassLoader classLoader;
    private final MaridContext context;
    private final Map<String, Bean> beanMap;
    private final Map<String, Class<?>> beanClasses = new HashMap<>();
    private final Set<String> creationBeanNames = new LinkedHashSet<>();
    private final IllegalStateException startException = new IllegalStateException("Runtime start exception");
    private final DefaultValueConvertersManager convertersManager;

    MaridBeanCreationContext(MaridConfiguration configuration, MaridContext context, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.context = context;
        this.beanMap = of(configuration.beans).collect(toMap(e -> e.name, identity()));
        this.convertersManager = new DefaultValueConvertersManager(
                classLoader,
                new MaridRuntime(this::getOrCreate, context::isActive, classLoader)
        );
    }

    Object getOrCreate(String name) {
        try {
            return context.beans.computeIfAbsent(name, this::create);
        } catch (Throwable x) {
            startException.addSuppressed(x);
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
            throw new IllegalStateException("Bean circular reference detected: " + name + " " + creationBeanNames);
        }
    }

    private Object create0(String name, Bean bean) throws Throwable {
        final BeanFactory factory = new BeanFactory(bean.factory);
        final Object factoryObject;
        final Class<?> factoryClass;
        if (factory.ref != null) {
            factoryObject = getOrCreate(factory.ref);
            factoryClass = beanClasses.get(factory.ref);
        } else {
            factoryClass = Class.forName(factory.factoryClass, true, classLoader);
            factoryObject = null;
        }
        final MaridFactoryBean factoryBean = new MaridFactoryBean(bean);
        final MethodHandle constructor = factoryBean.findProducer(factoryClass, factoryObject, factory.filter);

        beanClasses.put(name, constructor.type().returnType());
        final Class<?>[] argTypes = constructor.type().parameterArray();
        final Object[] args = new Object[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = arg(factoryBean, bean.args[i], argTypes[i]);
        }

        final Object instance = constructor.invokeWithArguments(args);
        if (instance != null) {
            final MethodHandle[] properties = factoryBean.findProperties(bean, constructor);
            for (int i = 0; i < bean.props.length; i++) {
                final MethodHandle setter = properties[i].bindTo(instance);
                final Object value = arg(factoryBean, bean.props[i], properties[i].type().parameterType(0));
                setter.invokeWithArguments(value);
            }
            context.initialize(name, instance);
        }
        return instance;
    }

    private Object arg(MaridFactoryBean bean, BeanMember member, Class<?> type) throws Throwable {
        final Object arg;
        if (member.value == null) {
            arg = MaridRuntimeUtils.defaultValue(type);
        } else {
            final Function<String, ?> argFunc = convertersManager.getConverter(member.type);
            if (argFunc != null) {
                arg = argFunc.apply(member.value);
            } else {
                throw new IllegalArgumentException("Unable to find converter for " + member.type);
            }
        }
        if (arg == null) {
            return null;
        }
        final int filterIndex = member.name.lastIndexOf('#');
        if (filterIndex < 0) {
            return arg;
        } else {
            final String filter = member.name.substring(filterIndex + 1);
            final MethodHandle argHandle = MethodHandles.constant(arg.getClass(), arg);
            return bean.filtered(filter, argHandle).invoke();
        }
    }

    @Override
    public void close() {
        if (startException.getSuppressed().length > 0) {
            try {
                context.close();
            } catch (Throwable x) {
                startException.addSuppressed(x);
            }
            throw startException;
        }
    }
}
