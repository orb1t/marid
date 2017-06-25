package org.marid.ide.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
@Lazy
public class DbConfiguration {

    @Bean
    public MongoClient mongoClient() {
        final MongoClientOptions.Builder options = new MongoClientOptions.Builder()
                .applicationName("Marid IDE")
                .description("Marid Mongo Connector");
        final MongoClientURI uri = new MongoClientURI("mongodb://marid:marid@ds131119.mlab.com:31119/marid", options);
        return new MongoClient(uri);
    }

    @Bean
    public MongoDatabase maridDb(MongoClient client, @Qualifier("marid") CodecRegistry codecRegistry) {
        return client.getDatabase("marid")
                .withCodecRegistry(codecRegistry);
    }
}
