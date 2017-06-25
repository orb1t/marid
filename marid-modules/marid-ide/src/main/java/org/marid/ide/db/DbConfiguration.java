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
