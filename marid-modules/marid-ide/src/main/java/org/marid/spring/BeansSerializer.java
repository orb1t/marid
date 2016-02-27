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

package org.marid.spring;

import org.marid.misc.Calls;
import org.marid.spring.xml.Beans;
import org.marid.xml.JaxbBiConsumer;
import org.marid.xml.JaxbBiFunction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeansSerializer {

    private static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";
    private static final JAXBContext CONTEXT = Calls.call(() -> JAXBContext.newInstance(Beans.class));

    private static String springSchemaLocation(String... schemas) {
        return Stream.of(schemas)
                .map(s -> format("%s%s %s%s/spring-%s.xsd", SPRING_SCHEMA_PREFIX, s, SPRING_SCHEMA_PREFIX, s, s))
                .collect(Collectors.joining(" "));
    }

    public static void serialize(Beans beans, OutputStream outputStream) throws IOException {
        serialize(outputStream, (m, o) -> m.marshal(beans, o));
    }

    public static void serialize(Beans beans, File file) throws IOException {
        serialize(file, (m, f) -> m.marshal(beans, f));
    }

    public static Beans deserialize(InputStream inputStream) throws IOException {
        return deserialize(inputStream, Unmarshaller::unmarshal);
    }

    public static Beans deserialize(File file) throws IOException {
        return deserialize(file, Unmarshaller::unmarshal);
    }

    private static <T> void serialize(T output, JaxbBiConsumer<Marshaller, T> task) throws IOException {
        try {
            final Marshaller marshaller = CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, springSchemaLocation("context", "util"));
            marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, springSchemaLocation("beans"));
            task.jaxbAccept(marshaller, output);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }

    private static <T> Beans deserialize(T input, JaxbBiFunction<Unmarshaller, T, Object> task) throws IOException {
        try {
            final Unmarshaller unmarshaller = CONTEXT.createUnmarshaller();
            return (Beans) task.jaxbApply(unmarshaller, input);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }
}
