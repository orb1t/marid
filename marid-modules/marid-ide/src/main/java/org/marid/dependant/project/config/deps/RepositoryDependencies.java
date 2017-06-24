/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.dependant.project.config.deps;

import com.mongodb.client.MongoDatabase;
import org.apache.maven.model.Dependency;
import org.marid.ide.model.Artifact;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Repository
public class RepositoryDependencies {

    private final MongoDatabase database;
    private final ProjectProfile profile;

    @Autowired
    public RepositoryDependencies(MongoDatabase maridDb, ProjectProfile profile) {
        this.profile = profile;
        this.database = maridDb;
    }

    public List<Dependency> getDependencies() {
        return StreamSupport.stream(database.getCollection("artifacts", Artifact.class).find().spliterator(), false)
                .filter(a -> !a.conf)
                .map(Artifact::toDependency)
                .collect(Collectors.toList());
    }

    public List<Dependency> getConfigurationDependencies() {
        return StreamSupport.stream(database.getCollection("artifacts", Artifact.class).find().spliterator(), false)
                .filter(a -> a.conf)
                .map(Artifact::toDependency)
                .collect(Collectors.toList());
    }
}
