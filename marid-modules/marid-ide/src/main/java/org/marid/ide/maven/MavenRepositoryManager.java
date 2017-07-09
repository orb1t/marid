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

package org.marid.ide.maven;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.marid.ide.common.Directories;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class MavenRepositoryManager implements AutoCloseable {

    final Path cacheDir;
    final Path indexDir;
    final BooleanProperty updating = new SimpleBooleanProperty();
    final DefaultPlexusContainer container;
    final Indexer indexer;
    final IndexUpdater indexUpdater;
    final Wagon wagon;
    final List<IndexCreator> indexCreators;
    final IndexingContext context;
    final ObjectFactory<MavenRepositoryService> service;

    @Autowired
    public MavenRepositoryManager(Directories directories,
                                  ObjectFactory<MavenRepositoryService> serviceFactory) throws Exception {
        service = serviceFactory;
        final Path dir = directories.getMarid().resolve("cache").resolve("repo");
        cacheDir = dir.resolve("cache");
        indexDir = dir.resolve("index");

        Files.createDirectories(cacheDir);
        Files.createDirectories(indexDir);

        final DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();
        configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);

        container = new DefaultPlexusContainer(configuration);
        indexer = container.lookup(Indexer.class);
        indexUpdater = container.lookup(IndexUpdater.class);
        wagon = container.lookup(Wagon.class, "http");

        indexCreators = ImmutableList.of(
                container.lookup(IndexCreator.class, "min"),
                container.lookup(IndexCreator.class, "jarContent")
        );

        context = indexer.createIndexingContext(
                "central-context",
                "central",
                cacheDir.toFile(),
                indexDir.toFile(),
                "http://repo1.maven.org/maven2",
                null,
                true,
                true,
                indexCreators
        );
    }

    public void update() {
        service.getObject().start();
    }

    public BooleanProperty updatingProperty() {
        return updating;
    }

    @EventListener
    public void onContextStart(ContextStartedEvent event) {
        update();
    }

    @Override
    public void close() throws Exception {
        container.dispose();
    }
}
