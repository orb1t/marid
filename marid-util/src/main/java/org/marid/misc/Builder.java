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

package org.marid.misc;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Builder<T> {

    final T builder;

    public Builder(T builder) {
        this.builder = builder;
    }

    public <A> Builder<T> set(BiConsumer<T, A> consumer, A arg) {
        consumer.accept(builder, arg);
        return this;
    }

    public T build() {
        return builder;
    }

    public static <T> T build(T arg, Consumer<T> consumer) {
        consumer.accept(arg);
        return arg;
    }
}
