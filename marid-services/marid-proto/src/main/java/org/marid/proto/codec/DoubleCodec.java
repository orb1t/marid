package org.marid.proto.codec;

import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class DoubleCodec implements Codec<Double> {

    private final int size;

    public DoubleCodec(int size) {
        this.size = size;
        if (size != 4 && size != 8) {
            throw new IllegalArgumentException("Incorrect size: " + size);
        }
    }

    @Override
    public Double decode(byte[] data) {
        switch (data.length) {
            case 4:
                return (double) ByteBuffer.wrap(data).getFloat(0);
            case 8:
                return ByteBuffer.wrap(data).getDouble(0);
            default:
                throw new IllegalArgumentException(Base64.getEncoder().encodeToString(data));
        }
    }

    @Override
    public byte[] encode(Double data) {
        switch (size) {
            case 4:
                return ByteBuffer.allocate(4).putFloat(0, data.floatValue()).array();
            case 8:
                return ByteBuffer.allocate(8).putDouble(0, data).array();
            default:
                throw new IllegalArgumentException("Size: " + size);
        }
    }
}
