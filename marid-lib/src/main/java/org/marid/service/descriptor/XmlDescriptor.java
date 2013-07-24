/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.service.descriptor;

import org.marid.data.MapValue;
import org.marid.xml.SubstitutedXmlEventReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "xml-descriptor")
@XmlSeeAlso(MapValue.class)
public class XmlDescriptor implements Descriptor {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String type;

    @XmlElement
    private MapValue parameters = new MapValue();

    @XmlTransient
    @Override
    public String getId() {
        return id == null ? getType() : id;
    }

    @Override
    public String getType() {
        if (type != null) {
            return type;
        } else {
            Package pkg = getClass().getPackage();
            if (pkg != null) {
                String[] pkgParts = pkg.getName().split("[.]");
                return pkgParts[pkgParts.length - 1];
            } else {
                return getClass().getSimpleName();
            }
        }
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters.getValue();
    }

    protected static <T extends Descriptor> T load(
            Class<T> descriptorType, Map<String, Object> bindings, URL url) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(descriptorType);
            Unmarshaller u = context.createUnmarshaller();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            try (Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                XMLEventReader eventReader = inputFactory.createXMLEventReader(r);
                XMLEventReader wrapper = new SubstitutedXmlEventReader(eventReader, bindings);
                return descriptorType.cast(u.unmarshal(wrapper));
            }
        } catch (JAXBException x) {
            throw new IllegalArgumentException(descriptorType.getName(), x);
        } catch (XMLStreamException x) {
            throw new IOException(x);
        }
    }
}
