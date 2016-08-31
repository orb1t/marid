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

package org.marid.jfx.util;

import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridStringConverter<T> extends StringConverter<T> {

    private final Function<T, String> decoder;
    private final Function<String, T> encoder;

    public MaridStringConverter(Function<T, String> decoder, Function<String, T> encoder) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public String toString(T object) {
        return decoder.apply(object);
    }

    @Override
    public T fromString(String string) {
        return encoder.apply(string);
    }
}
