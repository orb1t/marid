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

package org.marid.runtime.context2;

import org.marid.runtime.context.MaridRuntime;
import org.marid.runtime.event.ContextBootstrapEvent;
import org.marid.runtime.exception.MaridBeanNotFoundException;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.runtime.context.MaridRuntimeUtils.*;

public final class BeanContext implements MaridRuntime {

    private final BeanContext parent;
    private final BeanContextConfiguration configuration;
    private final MaridBean bean;
    private final Object instance;
    private final ArrayList<BeanContext> children = new ArrayList<>();
    private final HashSet<MaridBean> processing = new HashSet<>();

    public BeanContext(@Nullable BeanContext parent,
                       @Nonnull BeanContextConfiguration configuration,
                       @Nonnull MaridBean bean,
                       @Nonnull Supplier<Object> instance) {
        this.parent = parent;
        this.configuration = configuration;
        this.bean = bean;

        configuration.fireEvent(false, l -> l.bootstrap(new ContextBootstrapEvent(this)));

        for (final MaridBean child : bean.getChildren()) {
            if (children.stream().noneMatch(b -> b.bean.getName().equals(child.getName()))) {

            }
        }

        this.instance = instance.get();
    }

    public BeanContext(BeanContextConfiguration configuration, MaridBean root) {
        this(null, configuration, root, () -> null);
    }

    private Stream<BeanContext> descendants() {
        return children.stream().flatMap(c -> concat(of(c), c.descendants()));
    }

    @Override
    public Object getBean(String name) {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return configuration.getPlaceholderResolver().getClassLoader();
    }

    @Override
    public String resolvePlaceholders(String value) {
        return configuration.getPlaceholderResolver().resolvePlaceholders(value);
    }

    @Override
    public Properties getApplicationProperties() {
        return configuration.getPlaceholderResolver().getProperties();
    }

    @Override
    public Object getAscendant(String name) {
        if (parent == null) {
            throw new MaridBeanNotFoundException(name);
        } else {
            for (final MaridBean brother : parent.bean.getChildren()) {
                if (brother.getName().equals(name)) {
                    try {
                        return parent.children.stream()
                                .filter(c -> c.bean == brother)
                                .findFirst()
                                .map(c -> c.instance)
                                .orElseGet(() -> {
                                    final Supplier<Object> v = () -> parent.create(brother);
                                    final BeanContext c = new BeanContext(parent, configuration, brother, v);
                                    parent.children.add(c);
                                    return c.instance;
                                });
                    } catch (CircularBeanException x) {
                        // continue
                    }
                }
            }
            return parent.getAscendant(name);
        }
    }

    @Override
    public Object getDescendant(String name) {
        return descendants()
                .filter(c -> c.bean.getName().equals(name))
                .map(c -> c.instance)
                .findFirst()
                .orElseThrow(() -> new MaridBeanNotFoundException(name));
    }

    private Object create(MaridBean bean) {
        if (processing.add(bean)) {
            try {
                final Member member = fromSignature(bean.getSignature(), getClassLoader());
                final MethodHandle producer = producer(member);
                final MethodHandle producerHandle = isRoot(member)
                        ? producer.asFixedArity()
                        : producer.bindTo(getAscendant(bean.getFactory())).asFixedArity();
                return null;
            } finally {
                processing.remove(bean);
            }
        } else {
            throw new CircularBeanException();
        }
    }

    private static final class CircularBeanException extends RuntimeException {

        private CircularBeanException() {
            super(null, null, false, false);
        }
    }
}
