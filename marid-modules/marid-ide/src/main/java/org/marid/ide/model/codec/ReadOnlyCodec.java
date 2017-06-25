package org.marid.ide.model.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.function.BiFunction;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ReadOnlyCodec<T> implements Codec<T> {

    private final Class<T> encoderClass;
    private final BiFunction<BsonReader, DecoderContext, T> decoder;

    public ReadOnlyCodec(Class<T> encoderClass, BiFunction<BsonReader, DecoderContext, T> decoder) {
        this.encoderClass = encoderClass;
        this.decoder = decoder;
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        return decoder.apply(reader, decoderContext);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        throw new UnsupportedOperationException("Read-only");
    }

    @Override
    public Class<T> getEncoderClass() {
        return encoderClass;
    }
}
