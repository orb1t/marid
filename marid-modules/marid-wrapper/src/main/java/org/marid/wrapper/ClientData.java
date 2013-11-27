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

package org.marid.wrapper;

import org.marid.util.JaxbObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "client")
public class ClientData extends JaxbObject {

    @XmlElement
    private String user = "guest";

    @XmlElement
    private String password;

    @XmlElement
    private String javaVersion;

    @XmlTransient
    public String getUser() {
        return user;
    }

    public ClientData setUser(String user) {
        this.user = user;
        return this;
    }

    @XmlTransient
    public String getPassword() {
        return password;
    }

    public ClientData setPassword(String password) {
        this.password = password;
        return this;
    }

    @XmlTransient
    public String getJavaVersion() {
        return javaVersion;
    }

    public ClientData setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
        return this;
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("user", user);
        map.put("javaVersion", javaVersion);
        return getClass().getSimpleName() + map;
    }
}
