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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class XmlPersister implements LogSupport {

    private final Set<Class<?>> classes = new HashSet<>();
    private final JAXBContext context;
    private final Unmarshaller.Listener listener;

    @Autowired
    public XmlPersister(ConfigurableApplicationContext applicationContext) {
        for (final String beanName : applicationContext.getBeanNamesForAnnotation(XmlBindable.class)) {
            final BeanDefinition definition = applicationContext.getBeanFactory().getBeanDefinition(beanName);
            try {
                classes.add(Class.forName(definition.getBeanClassName(), false, Utils.currentClassLoader()));
            } catch (Exception x) {
                log(SEVERE, "Unable to load class {0}", definition.getBeanClassName());
            }
        }
        try {
            context = JAXBContext.newInstance(classes.toArray(new Class<?>[classes.size()]));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
        log(INFO, "XML classes: {0}", classes.stream().map(Class::getSimpleName).sorted().collect(Collectors.toList()));
        this.listener = new Unmarshaller.Listener() {
            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (classes.contains(target.getClass())) {
                    applicationContext.getAutowireCapableBeanFactory().autowireBean(target);
                    applicationContext.getAutowireCapableBeanFactory().initializeBean(target, null);
                }
            }
        };
    }

    public void save(Object object, StreamResult stream) throws IOException {
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(object, stream);
        } catch (JAXBException x) {
            if (x.getCause() instanceof IOException) {
                throw (IOException) x.getCause();
            } else {
                throw new IllegalStateException(x);
            }
        }
    }

    public <T> T load(Class<T> type, StreamSource source) throws IOException {
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setListener(listener);
            return type.cast(unmarshaller.unmarshal(source));
        } catch (JAXBException x) {
            if (x.getCause() instanceof IOException) {
                throw (IOException) x.getCause();
            } else {
                throw new IllegalStateException(x);
            }
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
