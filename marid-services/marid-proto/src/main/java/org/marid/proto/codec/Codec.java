package org.marid.proto.codec;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Codec<T> {

    T decode(byte[] data);

    byte[] encode(T data);
}
