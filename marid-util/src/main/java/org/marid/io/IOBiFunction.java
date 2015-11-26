/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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
import java.util.function.BiFunction;

/**
 * @author Dmitry Ovchinnikov.
 */
@FunctionalInterface
public interface IOBiFunction<T, U, R> extends BiFunction<T, U, R> {

    R ioApply(T arg1, U arg2) throws IOException;

    @Override
    default R apply(T t, U u) {
        try {
            return ioApply(t, u);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
}
