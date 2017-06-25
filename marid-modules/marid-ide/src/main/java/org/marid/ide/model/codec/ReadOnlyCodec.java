/*
 *
 */

package org.marid.ide.model.codec;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
