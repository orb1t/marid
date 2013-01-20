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
package org.marid.io;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

/**
 * Name read/write class.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class NameIo {

    /**
     * Reads a name.
     *
     * @param in Object input.
     * @return Name.
     * @throws IOException An I/O exception.
     */
    public static Name readName(ObjectInput in) throws
            IOException, ClassNotFoundException {
        try {
            if (in.readBoolean()) {
                return new CompositeName(in.readUTF());
            } else {
                return (Name) in.readObject();
            }
        } catch (InvalidNameException x) {
            throw new InvalidObjectException(x.toString(true));
        }
    }

    /**
     * Writes a name.
     *
     * @param out Object output.
     * @param n Name.
     * @throws IOException An I/O exception.
     */
    public static void writeName(ObjectOutput out, Name n) throws IOException {
        boolean flag = n instanceof CompositeName;
        out.writeBoolean(flag);
        if (flag) {
            out.writeUTF(n.toString());
        } else {
            out.writeObject(n);
        }
    }
}
