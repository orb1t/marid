/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.data.xml;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.marid.data.AbstractMapImporter;

/**
 * XML map importer.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class XmlMapImporter extends AbstractMapImporter {
	@Override
	public Map importMap(Reader r) throws IOException {
		try {
			JAXBContext ctx = JAXBContext.newInstance(XmlMapData.class);
			Unmarshaller u = ctx.createUnmarshaller();
			return ((XmlMapData)u.unmarshal(r)).getMap();
		} catch (JAXBException x) {
			throw new IOException(x);
		}
	}
}
