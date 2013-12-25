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

package org.marid.wrapper.data;

import org.marid.io.ser.SerializableObject;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClientData extends SerializableObject {

    private String user = "guest";

    private char[] password;

    private String javaVersion;

    public String getUser() {
        return user;
    }

    public ClientData setUser(String user) {
        this.user = user;
        return this;
    }

    public char[] password() {
        return password;
    }

    public ClientData password(char[] password) {
        this.password = password;
        return this;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public ClientData setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
        return this;
    }
}
