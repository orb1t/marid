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
