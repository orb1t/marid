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
package org.marid.data.xml.entries;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * XML abstract map entry.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
@XmlRootElement(name="entry")
@XmlSeeAlso({
	XmlIntEntry.class
})
public abstract class XmlAbstractMapEntry<T> {

	@XmlAttribute
	private final String key;

	/**
	 * Default constructor.
	 */
	public XmlAbstractMapEntry() {
		key = null;
	}

	/**
	 * Constructs the abstract map entry.
	 * @param k Entry key.
	 */
	public XmlAbstractMapEntry(String k) {
		key = k;
	}

	/**
	 * Get the entry key.
	 * @return Entry key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the entry value.
	 * @return Entry value.
	 */
	public abstract T getValue();
}
