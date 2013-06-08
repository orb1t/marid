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

package org.marid.service.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "services")
public class ServiceList {

    @XmlElement(name = "service")
    public final ArrayList<ServiceEntry> services = new ArrayList<>();

    @XmlRootElement
    public static class ServiceEntry {

        @XmlAttribute
        public String id;

        @XmlAttribute(name = "class")
        public String serviceClassName;

        @XmlAttribute(name = "supplier")
        public String supplierClassName;

        @XmlAttribute
        public String type;

        @XmlAttribute
        public String descriptor;

        @Override
        public String toString() {
            Object[] array = {id, type, descriptor, serviceClassName, supplierClassName};
            return Arrays.toString(array);
        }
    }
}
