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
