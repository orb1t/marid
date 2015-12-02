/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.xml;

import org.marid.logging.LogSupport;
import org.marid.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.Introspector;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Character.MAX_RADIX;
import static java.lang.Integer.toUnsignedString;
import static java.lang.System.identityHashCode;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class XmlPersister extends Unmarshaller.Listener implements LogSupport {

    private final ConfigurableApplicationContext applicationContext;
    private final Set<Class<?>> classes;
    private final JAXBContext context;

    @Autowired
    public XmlPersister(ConfigurableApplicationContext applicationContext) throws JAXBException {
        this.applicationContext = applicationContext;
        final String[] beanNames = applicationContext.getBeanNamesForAnnotation(XmlBindable.class);
        classes = new HashSet<>(beanNames.length);
        for (final String beanName : beanNames) {
            final BeanDefinition definition = applicationContext.getBeanFactory().getBeanDefinition(beanName);
            try {
                classes.add(Class.forName(definition.getBeanClassName(), false, Utils.currentClassLoader()));
            } catch (Exception x) {
                log(SEVERE, "Unable to load class {0}", definition.getBeanClassName());
            }
        }
        context = JAXBContext.newInstance(classes.toArray(new Class<?>[classes.size()]));
        log(INFO, "XML classes: {0}", classes.stream().map(Class::getSimpleName).sorted().collect(Collectors.toList()));
    }

    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (classes.contains(target.getClass())) {
            applicationContext.getAutowireCapableBeanFactory().autowireBean(target);
            final String beanName = Introspector.decapitalize(target.getClass().getSimpleName());
            final String name = beanName + "@" + toUnsignedString(identityHashCode(target), MAX_RADIX);
            applicationContext.getAutowireCapableBeanFactory().initializeBean(target, name);
        }
    }

    public void save(Object object, StreamResult stream) {
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(object, stream);
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public <T> T load(Class<T> type, StreamSource source) {
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setListener(this);
            return type.cast(unmarshaller.unmarshal(source));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public JAXBContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "XmlPersister(" + classes.size() + ")";
    }
}
