/*
 *
 */

package org.marid.proto.codec;

/*-
 * #%L
 * marid-proto
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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

    static Consumer<byte[]> decoder(Consumer<byte[]> target, TwoBytesOrder order) {
        return src -> {
            final byte[] dst = new byte[src.length];
            for (int i = 0; i < src.length; i += 2) {
                final byte[] source = {src[i], src[i + 1]};
                final byte[] dest = order.decode(source);
                dst[i] = dest[0];
                dst[i + 1] = dest[1];
            }
            target.accept(dst);
        };
    }
}
