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
