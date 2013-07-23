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
import org.marid.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.marid.Scripting.SCRIPTING;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters.getValue();
    }

    static <T extends Descriptor> T load(
            Class<T> descriptorType, Map<String, Object> bindings, URL url) throws IOException {
        try {
            JAXBContext context = JAXBContext.newInstance(descriptorType);
            Unmarshaller u = context.createUnmarshaller();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            try (Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                XMLEventReader eventReader = inputFactory.createXMLEventReader(r);
                XmlEventReader wrapper = new XmlEventReader(eventReader, bindings);
                return descriptorType.cast(u.unmarshal(wrapper));
            }
        } catch (JAXBException x) {
            throw new IllegalArgumentException(descriptorType.getName(), x);
        } catch (XMLStreamException x) {
            throw new IOException(x);
        }
    }

    private static class XmlEventReader implements XMLEventReader {

        private final XMLEventReader delegate;
        private final Map<String, Object> bindings;

        public XmlEventReader(XMLEventReader delegate, Map<String, Object> bindings) {
            this.delegate = delegate;
            this.bindings = bindings;
        }

        @Override
        public XMLEvent nextEvent() throws XMLStreamException {
            return transform(delegate.nextEvent());
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Object next() {
            Object v = delegate.next();
            if (v instanceof XMLEvent) {
                return transform((XMLEvent) v);
            }
            return v;
        }

        @Override
        public void remove() {
            delegate.remove();
        }

        @Override
        public XMLEvent peek() throws XMLStreamException {
            return transform(delegate.peek());
        }

        @Override
        public String getElementText() throws XMLStreamException {
            String v = delegate.getElementText();
            return v == null ? null : SCRIPTING.replace(v, bindings);
        }

        @Override
        public XMLEvent nextTag() throws XMLStreamException {
            return transform(delegate.nextTag());
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return delegate.getProperty(name);
        }

        @Override
        public void close() throws XMLStreamException {
            delegate.close();
        }

        private XMLEvent transform(final XMLEvent ev) {
            if (ev == null) {
                return null;
            } else if (ev.isAttribute()) {
                if (ev instanceof Attribute) {
                    return new DelegatedAttribute((Attribute) ev);
                } else {
                    return ev;
                }
            } else if (ev.isStartElement()) {
                return new XmlStartElement(ev.asStartElement());
            } else if (ev.isCharacters()) {
                return new XmlCharacters(ev.asCharacters());
            } else {
                return ev;
            }
        }

        private class XmlEvent<D extends XMLEvent> implements XMLEvent {

            protected final D delegate;

            public XmlEvent(D delegate) {
                this.delegate = delegate;
            }

            @Override
            public int getEventType() {
                return delegate.getEventType();
            }

            @Override
            public Location getLocation() {
                return delegate.getLocation();
            }

            @Override
            public boolean isStartElement() {
                return delegate.isStartElement();
            }

            @Override
            public boolean isAttribute() {
                return delegate.isAttribute();
            }

            @Override
            public boolean isNamespace() {
                return delegate.isNamespace();
            }

            @Override
            public boolean isEndElement() {
                return delegate.isEndElement();
            }

            @Override
            public boolean isEntityReference() {
                return delegate.isEntityReference();
            }

            @Override
            public boolean isProcessingInstruction() {
                return delegate.isProcessingInstruction();
            }

            @Override
            public boolean isCharacters() {
                return delegate.isCharacters();
            }

            @Override
            public boolean isStartDocument() {
                return delegate.isStartDocument();
            }

            @Override
            public boolean isEndDocument() {
                return delegate.isEndDocument();
            }

            @Override
            public StartElement asStartElement() {
                return new XmlStartElement(delegate.asStartElement());
            }

            @Override
            public EndElement asEndElement() {
                return delegate.asEndElement();
            }

            @Override
            public Characters asCharacters() {
                return new XmlCharacters(delegate.asCharacters());
            }

            @Override
            public QName getSchemaType() {
                return delegate.getSchemaType();
            }

            @Override
            public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
                delegate.writeAsEncodedUnicode(writer);
            }
        }

        private class DelegatedAttribute extends XmlEvent<Attribute> implements Attribute {

            public DelegatedAttribute(Attribute delegate) {
                super(delegate);
            }

            @Override
            public QName getName() {
                return delegate.getName();
            }

            @Override
            public String getValue() {
                String v = delegate.getValue();
                return v == null ? null : SCRIPTING.replace(v, bindings);
            }

            @Override
            public String getDTDType() {
                return delegate.getDTDType();
            }

            @Override
            public boolean isSpecified() {
                return delegate.isSpecified();
            }
        }

        private class XmlStartElement extends XmlEvent<StartElement> implements StartElement {

            public XmlStartElement(StartElement delegate) {
                super(delegate);
            }

            @Override
            public QName getName() {
                return delegate.getName();
            }

            @Override
            public Iterator getAttributes() {
                final Iterator it = delegate.getAttributes();
                if (it != null) {
                    return new Iterator() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Object next() {
                            Object v = it.next();
                            if (v instanceof Attribute) {
                                return new DelegatedAttribute((Attribute) v);
                            } else {
                                return v;
                            }
                        }

                        @Override
                        public void remove() {
                            it.remove();
                        }
                    };
                } else {
                    return it;
                }
            }

            @Override
            public Iterator getNamespaces() {
                return delegate.getNamespaces();
            }

            @Override
            public Attribute getAttributeByName(QName name) {
                Attribute attribute = delegate.getAttributeByName(name);
                return attribute != null ? new DelegatedAttribute(attribute) : attribute;
            }

            @Override
            public NamespaceContext getNamespaceContext() {
                return delegate.getNamespaceContext();
            }

            @Override
            public String getNamespaceURI(String prefix) {
                return delegate.getNamespaceURI(prefix);
            }
        }

        private class XmlCharacters extends XmlEvent<Characters> implements Characters {

            public XmlCharacters(Characters delegate) {
                super(delegate);
            }

            @Override
            public String getData() {
                String v = delegate.getData();
                return v == null ? v : SCRIPTING.replace(v, bindings);
            }

            @Override
            public boolean isWhiteSpace() {
                return delegate.isWhiteSpace();
            }

            @Override
            public boolean isCData() {
                return delegate.isCData();
            }

            @Override
            public boolean isIgnorableWhiteSpace() {
                return delegate.isIgnorableWhiteSpace();
            }
        }
    }
}
