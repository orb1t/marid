package org.marid.proto.codec;

/**
 * @author Dmitry Ovchinnikov
 */
public enum TwoBytesOrder implements Codec<byte[]> {

    ABCD(0, 1, 2, 3),
    BADC(1, 0, 3, 2),
    DCBA(3, 2, 1, 0),
    CDAB(2, 3, 0, 1);

    private final int[] indices;

    TwoBytesOrder(int... indices) {
        this.indices = indices;
    }

    @Override
    public byte[] decode(byte[] data) {
        final byte[] res = new byte[4];
        for (int i = 0; i < indices.length; i++) {
            res[i] = data[indices[i]];
        }
        return res;
    }

    @Override
    public byte[] encode(byte[] data) {
        final byte[] res = new byte[4];
        for (int i = 0; i < indices.length; i++) {
            res[indices[i]] = data[i];
        }
        return res;
    }
}
