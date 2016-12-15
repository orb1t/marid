/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.beans;

import org.marid.logging.LogSupport;

import java.awt.*;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class MaridBeanInfo extends SimpleBeanInfo implements LogSupport {

    protected final BeanDescriptor descriptor;

    public MaridBeanInfo(Class<?> beanClass, Class<?> customizerClass) {
        descriptor = new BeanDescriptor(beanClass, customizerClass);
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        return descriptor;
    }

    @Override
    public Image getIcon(int iconKind) {
        final BeanDescriptor descriptor = getBeanDescriptor();
        if (descriptor == null) {
            return null;
        }
        switch (iconKind) {
            case ICON_COLOR_16x16:
            case ICON_MONO_16x16: {
                final URL url = (URL) descriptor.getValue("icon16");
                return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
            }
            case ICON_COLOR_32x32:
            case ICON_MONO_32x32: {
                final URL url = (URL) descriptor.getValue("icon32");
                return url == null ? null : Toolkit.getDefaultToolkit().getImage(url);
            }
            default:
                return null;
        }
    }

    @Override
    public Image loadImage(String resourceName) {
        final URL url = getClass().getResource(resourceName);
        if (url == null) {
            return null;
        }
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    public final class PropertyDescriptorsBuilder {

        private final ArrayList<PropertyDescriptor> propertyDescriptors = new ArrayList<>();

        @SafeVarargs
        public final PropertyDescriptorsBuilder add(String name, Consumer<PropertyDescriptor>... consumers) {
            try {
                final Class<?> beanClass = getBeanDescriptor().getBeanClass();
                final Set<String> getters = Stream.of("is", "get").map(s -> s + name.toLowerCase()).collect(toSet());
                final Set<String> setters = Stream.of("set").map(s -> s + name.toLowerCase()).collect(toSet());
                final Method readMethod = Stream.of(beanClass.getMethods())
                        .filter(m -> getters.contains(m.getName().toLowerCase()))
                        .findFirst()
                        .orElse(null);
                final Method writeMethod = Stream.of(beanClass.getMethods())
                        .filter(m -> setters.contains(m.getName().toLowerCase()))
                        .findFirst()
                        .orElse(null);
                if (readMethod == null && writeMethod == null) {
                    return this;
                }
                final PropertyDescriptor descriptor = new PropertyDescriptor(name, readMethod, writeMethod);
                Stream.of(consumers).forEach(c -> c.accept(descriptor));
                propertyDescriptors.add(descriptor);
            } catch (IntrospectionException x) {
                log(WARNING, "Unable to add descriptor for property {0}", x, name);
            }
            return this;
        }

        public final PropertyDescriptor[] build() {
            return propertyDescriptors.stream().toArray(PropertyDescriptor[]::new);
        }
    }
}
