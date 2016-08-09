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

import org.marid.spring.xml.data.BeanFile;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanDefinitionLoader {

    public static BeanFile load(Path path) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        }
    }

    public static BeanFile load(InputStream stream) throws IOException {
        try {
            final Unmarshaller unmarshaller = MaridBeanDefinitionSaver.CONTEXT.createUnmarshaller();
            return (BeanFile) unmarshaller.unmarshal(stream);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }
}
