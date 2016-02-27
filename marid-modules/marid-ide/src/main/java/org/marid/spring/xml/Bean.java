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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bean")
public class Bean {

    @XmlAttribute(name = "abstract")
    public Boolean abstractBean;

    @XmlAttribute(name = "scope")
    public String scope;

    @XmlAttribute(name = "autowire")
    public String autowire;

    @XmlAttribute(name = "autowire-candidate")
    public String autowireCandidate;

    @XmlAttribute(name = "class")
    public String beanClass;

    @XmlAttribute(name = "lazy-init")
    public String lazyInit;

    @XmlAttribute(name = "primary")
    public Boolean primary;

    @XmlAttribute(name = "parent")
    public String parent;

    @XmlAttribute(name = "depends-on")
    public List<String> dependsOn;

    @XmlAttribute(name = "name")
    public String name;

    @XmlAttribute(name = "id")
    public String id;

    @XmlAttribute(name = "init-method")
    public String initMethod;

    @XmlAttribute(name = "destroy-method")
    public String destroyMethod;

    @XmlAttribute(name = "factory-bean")
    public String factoryBean;

    @XmlAttribute(name = "factory-method")
    public String factoryMethod;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
