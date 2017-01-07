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

package org.marid.spring.xml;

import org.marid.misc.Calls;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Path;

import static javax.xml.bind.Marshaller.JAXB_ENCODING;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionSaver {

    static final JAXBContext CONTEXT = Calls.call(() -> JAXBContext.newInstance(BeanFile.class));

    public static void write(Path path, BeanFile beanFile) throws IOException {
        write(new StreamResult(path.toFile()), beanFile);
    }

    public static void write(Result result, BeanFile beanFile) throws IOException {
        try {
            final Marshaller marshaller = CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(JAXB_ENCODING, "UTF-8");
            final QName name = new QName("http://www.springframework.org/schema/beans", "beans");
            final JAXBElement<BeanFile> element = new JAXBElement<>(name, BeanFile.class, beanFile);
            marshaller.marshal(element, result);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }
}
