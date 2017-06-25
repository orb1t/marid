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
