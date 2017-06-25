package org.marid.proto.codec;

import java.nio.ByteBuffer;

/**
 * @author Dmitry Ovchinnikov
 */
public class IntCodec implements Codec<Integer> {

    @Override
    public Integer decode(byte[] data) {
        return ByteBuffer.wrap(data).getInt(0);
    }

    @Override
    public byte[] encode(Integer data) {
        return ByteBuffer.allocate(4).putInt(0, data).array();
    }
}
