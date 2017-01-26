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

package org.marid.ide.model;

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
