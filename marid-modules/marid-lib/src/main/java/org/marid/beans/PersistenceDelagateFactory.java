/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.beans;

import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public interface PersistenceDelagateFactory {

    Map<Class<?>, PersistenceDelegate> getPersistenceDelegateMap();

    public static <T> T read(Class<T> type, InputStream inputStream) {
        try (final XMLDecoder decoder = new XMLDecoder(inputStream)) {
            return type.cast(decoder.readObject());
        }
    }

    public static void write(OutputStream outputStream, Object object) {
        try (final XMLEncoder encoder = new XMLEncoder(outputStream, "UTF-8", true, 0)) {
            for (final PersistenceDelagateFactory factory : ServiceLoader.load(PersistenceDelagateFactory.class)) {
                factory.getPersistenceDelegateMap().forEach(encoder::setPersistenceDelegate);
            }
            encoder.writeObject(object);
        }
    }
}
