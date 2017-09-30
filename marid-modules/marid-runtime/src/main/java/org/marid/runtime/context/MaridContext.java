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
import org.marid.runtime.event.*;
import org.marid.runtime.exception.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;
import static org.marid.runtime.context.MaridRuntimeUtils.*;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridContext implements MaridRuntime, AutoCloseable {

    private final MaridConfiguration configuration;
    private final String name;
    private final Object value;
    private final MaridContext parent;
    private final List<MaridContext> children;

    MaridContext(MaridConfiguration conf, MaridContext p, Creator pcc, Bean bean, Function<Creator, Object> v) {
        this.name = bean.name;
        this.parent = p;
        this.configuration = conf;
        this.children = new ArrayList<>(bean.children.size());

        try (final Creator creator = new Creator(pcc, bean)) {
            conf.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this)));
            for (final Bean b : bean.children) {
                if (children.stream().noneMatch(c -> c.name.equals(b.name))) {
                    children.add(child(creator, b, c -> c.create(b)));
                }
            }
            this.value = v.apply(creator);
            conf.fireEvent(false, l -> l.onStart(new ContextStartEvent(this)));
        } catch (Throwable x) {
            close();
            conf.fireEvent(false, l -> l.onFail(new ContextFailEvent(this, name, x)));
            throw x;
        }
    }

    public MaridContext(Bean root, ClassLoader classLoader, Properties systemProperties) {
        this(new MaridConfiguration(classLoader, systemProperties), null, null, root, c -> null);
    }

    public MaridContext(Bean root) {
        this(root, Thread.currentThread().getContextClassLoader(), System.getProperties());
    }

    private MaridContext child(Creator pcc, Bean bean, Function<Creator, Object> v) {
        return new MaridContext(configuration, this, pcc, bean, v);
    }

    @Override
    public MaridContext getParent() {
        return parent;
    }

    public List<MaridContext> getChildren() {
        return children;
    }

    @Override
    public Object getBean(String name) {
        for (final MaridContext child : children) {
            if (child.name.equals(name)) {
                return child.value;
            }
        }
        if (parent == null) {
            throw new MaridBeanNotFoundException(name);
        } else {
            return parent.getBean(name);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return configuration.getClassLoader();
    }

    @Override
    public String resolvePlaceholders(String value) {
        return configuration.placeholderResolver.resolvePlaceholders(value);
    }

    @Override
    public Properties getApplicationProperties() {
        return configuration.placeholderResolver.getProperties();
    }

    private void initialize(String name, Object bean) {
        configuration.fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_INIT")));
        configuration.fireEvent(false, l -> l.onPostConstruct(new BeanPostConstructEvent(this, name, bean)));
        configuration.fireEvent(false, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_INIT")));
    }

    private void destroy(String name, Object bean, Consumer<Throwable> errorConsumer) {
        configuration.fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "PRE_DESTROY")));
        configuration.fireEvent(true, l -> l.onPreDestroy(new BeanPreDestroyEvent(this, name, bean, errorConsumer)));
        configuration.fireEvent(true, l -> l.onEvent(new BeanEvent(this, name, bean, "POST_DESTROY")));
    }

    @Override
    public void close() {
        final IllegalStateException e = new IllegalStateException("Runtime close exception");
        for (int i = children.size() - 1; i >= 0; i--) {
            final MaridContext child = children.get(i);
            destroy(child.name, child.value, e::addSuppressed);
        }
        configuration.fireEvent(true, l -> l.onStop(new ContextStopEvent(this)));
        if (e.getSuppressed().length > 0) {
            log(WARNING, "Error on close {0}", e, this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(name);
        for (MaridContext c = parent; c != null; c = c.parent) {
            builder.insert(0, c.name + "/");
        }
        return builder.toString();
    }

    private final class Creator implements AutoCloseable {

        private final Creator parent;
        private final Bean bean;
        private final DefaultValueConvertersManager convertersManager;
        private final LinkedHashSet<String> processing = new LinkedHashSet<>();

        private Creator(Creator parent, Bean bean) {
            this.parent = parent;
            this.bean = bean;
            this.convertersManager = new DefaultValueConvertersManager(MaridContext.this);
        }

        private Object getOrCreate(String name) {
            try {
                return getBean(name);
            } catch (MaridBeanNotFoundException x) {
                try {
                    return create(name);
                } catch (RuntimeException rx) {
                    throw rx;
                } catch (Throwable tx) {
                    throw new MaridBeanInitializationException(name, tx);
                }
            }
        }

        private Object create(String name) {
            final Optional<Bean> bo = bean.children.stream()
                    .filter(e -> e.name.equals(name))
                    .filter(e -> processing.add(e.name))
                    .findFirst();
            if (bo.isPresent()) {
                final Bean b = bo.get();
                try {
                    final MaridContext c = child(this, b, cc -> cc.create(b));
                    children.add(c);
                    return c.value;
                } finally {
                    processing.remove(name);
                }
            }
            if (parent != null) {
                return parent.create(name);
            } else {
                throw new MaridBeanNotFoundException(name);
            }
        }

        private Object create(Bean bean) {
            final Member factory = fromSignature(bean.signature, getClassLoader());
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
                    final Member member = fromSignature(initializer.signature, getClassLoader());
                    final MethodHandle handle = bind(initializer, initializer(member), instance);
                    final Class<?>[] argTypes = handle.type().parameterArray();
                    final Object[] args = new Object[argTypes.length];
                    for (int i = 0; i < args.length; i++) {
                        args[i] = arg(bean, initializer, initializer.args[i], argTypes[i]);
                    }
                    invoke(bean, initializer, handle, args);
                }
                initialize(bean.name, instance);
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
                        return converter.convert(resolvePlaceholders(methodArg.value), Casts.cast(type));
                    }
                }
            } catch (RuntimeException x) {
                throw x;
            } catch (Throwable x) {
                throw new MaridBeanMethodArgException(bean.name, method.signature, methodArg.index(method), x);
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
}
