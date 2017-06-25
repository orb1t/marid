/*
 *
 */

package org.marid.ide.db;

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

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.marid.ide.model.Artifact;
import org.marid.ide.model.codec.ReadOnlyCodec;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
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
            final boolean conf = r.readBoolean("conf");
            r.readEndDocument();
            return new Artifact(groupId, artifactId, version, conf);
        });
    }

    @Bean
    @Qualifier("marid")
    public CodecRegistry codecRegistry(@Qualifier("marid") List<Codec<?>> codecs) {
        return CodecRegistries.fromCodecs(codecs);
    }
}
