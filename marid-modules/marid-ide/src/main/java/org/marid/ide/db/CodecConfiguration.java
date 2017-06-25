package org.marid.ide.db;

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
