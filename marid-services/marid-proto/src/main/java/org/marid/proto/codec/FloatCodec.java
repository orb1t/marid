package org.marid.proto.codec;

import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov
 */
public class FloatCodec implements Codec<Float> {

    @Override
    public Float decode(byte[] data) {
        return ByteBuffer.wrap(data).getFloat(0);
    }

    @Override
    public byte[] encode(Float data) {
        return ByteBuffer.allocate(4).putFloat(0, data).array();
    }
}
