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
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "auth-response")
public class AuthResponse extends JaxbObject {

    @XmlElement
    private final String result;

    @XmlElement
    private final TreeSet<String> roles;

    public AuthResponse(String result, Set<String> roles) {
        this.result = result;
        this.roles = new TreeSet<>(roles);
    }

    public AuthResponse(String result, String... roles) {
        this(result, new HashSet<>(Arrays.asList(roles)));
    }

    public AuthResponse(String result) {
        this(result, Collections.<String>emptySet());
    }

    public AuthResponse() {
        this(null);
    }

    public String getResult() {
        return result;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
