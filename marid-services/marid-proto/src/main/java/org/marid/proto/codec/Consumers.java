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

package org.marid.proto.codec;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public interface Consumers {

    static Consumer<byte[]> doubleConsumer(Consumer<Double> consumer, Codec<Double> doubleCodec) {
        return data -> consumer.accept(doubleCodec.decode(data));
    }

    static Consumer<byte[]> floatConsumer(Consumer<Float> consumer, Codec<Float> floatCodec) {
        return data -> consumer.accept(floatCodec.decode(data));
    }

    static Consumer<byte[]> intConsumer(Consumer<Integer> consumer, Codec<Integer> integerCodec) {
        return data -> consumer.accept(integerCodec.decode(data));
    }
}
