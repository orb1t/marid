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

package org.marid.ide.db;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.marid.ide.model.Artifact;
import org.marid.ide.model.codec.ReadOnlyCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@Lazy
public class CodecConfiguration {

    @Bean
    @Qualifier("marid")
    public Codec<Artifact> artifactCodec() {
        return new ReadOnlyCodec<>(Artifact.class, (r, c) -> {
            r.readStartDocument();
            r.readInt32("_id");
            final String artifactId = r.readString("artifact-id");
            final String groupId = r.readString("group-id");
            final String version = r.readString("version");
            final boolean hmi = r.readBoolean("hmi");
            final boolean conf = r.readBoolean("conf");
            r.readEndDocument();
            return new Artifact(groupId, artifactId, version, hmi, conf);
        });
    }

    @Bean
    @Qualifier("marid")
    public CodecRegistry codecRegistry(@Qualifier("marid") List<Codec<?>> codecs) {
        return CodecRegistries.fromCodecs(codecs);
    }
}
