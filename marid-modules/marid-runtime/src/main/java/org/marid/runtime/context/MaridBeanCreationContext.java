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
import org.marid.runtime.beans.BeanMember;
import org.marid.runtime.converter.DefaultValueConvertersManager;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.defaultValue;

/**
 * @author Dmitry Ovchinnikov
 */
final class MaridBeanCreationContext implements AutoCloseable {

    private final ClassLoader classLoader;
    private final MaridContext context;
    private final Map<String, Bean> beanMap;
    private final Map<String, Class<?>> beanClasses = new HashMap<>();
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
        return context.beans.computeIfAbsent(name, this::create);
    }

    private Object create(String name) {
        final Bean bean = requireNonNull(beanMap.get(name), () -> "No such bean " + name);
        final Object factoryObject;
        final Class<?> factoryClass;
        if (bean.factory.startsWith("@")) {
            final String beanName = bean.factory.substring(1);
            factoryObject = getOrCreate(beanName);
            factoryClass = beanClasses.get(beanName);
        } else {
            try {
                factoryClass = Class.forName(bean.factory, true, classLoader);
                factoryObject = null;
            } catch (ClassNotFoundException x1) {
                throw new IllegalStateException(x1);
            }
        }
        final MaridFactoryBean factoryBean = new MaridFactoryBean(bean);
        final MethodHandle constructor = factoryBean.findProducer(factoryClass, factoryObject);
        beanClasses.put(name, constructor.type().returnType());
        final Class<?>[] argClasses = constructor.type().parameterArray();
        final Object[] args = IntStream.range(0, argClasses.length)
                .mapToObj(i -> bean.args[i].value == null ? defaultValue(argClasses[i]) : arg(bean.args[i]))
                .toArray();

        try {
            final Object instance = constructor.invokeWithArguments(args);
            if (instance != null) {
                final MethodHandle[] properties = bean.findProperties(constructor);
                for (int i = 0; i < bean.props.length; i++) {
                    final MethodHandle setter = properties[i].bindTo(instance);
                    final Object value = arg(bean.props[i]);
                    try {
                        setter.invokeWithArguments(value);
                    } catch (Throwable x) {
                        throw new IllegalArgumentException("Unable to apply " + value + " to " + setter, x);
                    }
                }
                context.initialize(name, instance);
            }
            return instance;
        } catch (RuntimeException x) {
            throw x;
        } catch (Throwable x) {
            throw new IllegalStateException(x);
        }
    }

    private Object arg(BeanMember member) {
        final Function<String, ?> argFunc = convertersManager.getConverter(member.type);
        if (argFunc != null) {
            return argFunc.apply(member.value);
        } else {
            throw new IllegalArgumentException("Unable to find converter for " + member.type);
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
