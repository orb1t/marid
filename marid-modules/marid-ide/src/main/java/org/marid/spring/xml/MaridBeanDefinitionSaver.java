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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionSaver {

    static final JAXBContext CONTEXT = Calls.call(() -> JAXBContext.newInstance(BeanFile.class));

    public static void write(Path path, BeanFile beanFile) throws IOException {
        try (final OutputStream outputStream = Files.newOutputStream(path)) {
            write(outputStream, beanFile);
        }
    }

    public static void write(OutputStream outputStream, BeanFile beanFile) throws IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            final Marshaller marshaller = CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(beanFile, outputStream);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }
}
