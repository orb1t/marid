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
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdater;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.marid.ide.model.MavenArtifact;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class MavenRepositoryManager implements AutoCloseable {

    private final ObjectFactory<MavenRepositoryService> service;
    private final List<IndexingContext> contexts = new ArrayList<>();

    final Indexer indexer;
    final DefaultPlexusContainer container;
    final IndexUpdater indexUpdater;

    @Autowired
    public MavenRepositoryManager(ObjectFactory<MavenRepositoryService> serv, MavenRepositories repositories) throws Exception {
        service = serv;

        final DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();
        configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);

        container = new DefaultPlexusContainer(configuration);
        indexer = container.lookup(Indexer.class);
        indexUpdater = container.lookup(IndexUpdater.class);

        final List<IndexCreator> min = ImmutableList.of(container.lookup(IndexCreator.class, "min"));

        for (final MavenRepository r : repositories.getRepositories()) {
            final Path base = repositories.getBaseDir().resolve(r.id);

            final File index = base.resolve("index").toFile();
            if (index.mkdirs()) {
                log(INFO, "Created {0}", index);
            }
            if (r.directory.mkdirs()) {
                log(INFO, "Created {0}", r.directory);
            }

            contexts.add(indexer.createIndexingContext(r.id, r.id, r.directory, index, r.url, null, true, true, min));
        }
    }

    @IdeAction
    public Spliterator<FxAction> repositoryActions() {
        final LinkedList<FxAction> list = new LinkedList<>();
        for (final IndexingContext context : contexts) {
            final SimpleBooleanProperty disabledProperty = new SimpleBooleanProperty();
            final FxAction action = new FxAction("repo", "Repositories")
                    .bindDisabled(disabledProperty)
                    .bindText(ls("%s repository [%s]", ls("Update"), context.getRepositoryId()))
                    .setEventHandler(event -> {
                        final MavenRepositoryService service = this.service.getObject().setContext(context);
                        disabledProperty.bind(service.runningProperty());
                        service.start();
                    });
            list.add(action);
        }
        return list.spliterator();
    }

    public Stream<MavenArtifact> getMaridArtifacts(@Nonnull Function<Indexer, BooleanQuery> queryFunction) {
        final IteratorSearchRequest request = new IteratorSearchRequest(queryFunction.apply(indexer), contexts);
        try (final IteratorSearchResponse response = indexer.searchIterator(request)) {
            return StreamSupport.stream(response.spliterator(), false)
                    .map(e -> new MavenArtifact(e.getGroupId(), e.getArtifactId(), e.getVersion()));
        } catch (Exception x) {
            log(WARNING, "Unable to fetch artifacts", x);
            return Stream.empty();
        }
    }

    @Override
    public void close() throws Exception {
        try {
            for (final IndexingContext context : contexts) {
                try {
                    context.close(false);
                } catch (Exception x) {
                    log(WARNING, "Unable to close {0}", x, context.getId());
                }
            }
        } finally {
            container.dispose();
        }
    }
}
