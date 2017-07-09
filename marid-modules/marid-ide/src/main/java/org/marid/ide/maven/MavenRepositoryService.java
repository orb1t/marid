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

import javafx.scene.layout.HBox;
import org.apache.maven.index.*;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.WagonHelper.WagonFetcher;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.marid.ide.status.IdeService;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.logging.Level.*;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class MavenRepositoryService extends IdeService<HBox> {

    private final MavenRepositoryManager manager;

    private IndexingContext context;

    @Autowired
    public MavenRepositoryService(MavenRepositoryManager manager) {
        this.manager = manager;

        setOnRunning(event -> manager.updating.set(true));
        setOnCancelled(event -> manager.updating.set(false));
        setOnFailed(event -> manager.updating.set(false));
        setOnSucceeded(event -> manager.updating.set(false));
    }

    public MavenRepositoryService setContext(IndexingContext context) {
        this.context = context;
        return this;
    }

    @Override
    protected IdeTask createTask() {
        return new RepositoryUpdateTask();
    }

    public class RepositoryUpdateTask extends IdeTask {

        @Override
        protected void execute() throws Exception {
            updateTitle(s("%s Update", context.getId()));
            log(INFO, "Repo {0} last updated {1}", context.getId(), context.getTimestamp());
            if (context.getRepositoryUrl() == null) {
                final Scanner scanner = manager.container.lookup(Scanner.class);
                final ArtifactListener listener = new ArtifactListener();
                final ScanningRequest request = new ScanningRequest(context, listener);
                final ScanningResult result = scanner.scan(request);
                log(INFO, "Scanned {0} files", result.getTotalFiles());
                manager.indexer.addArtifactsToIndex(listener.artifactInfos, context);
            } else {
                final Wagon wagon = manager.container.lookup(Wagon.class, "http");
                final WagonFetcher fetcher = new WagonFetcher(wagon, new WagonListener(), null, null);
                try {
                    final IndexUpdateRequest request = new IndexUpdateRequest(context, fetcher);
                    final IndexUpdateResult result = manager.indexUpdater.fetchAndUpdateIndex(request);
                    if (result.isFullUpdate()) {
                        log(INFO, "Full index update succeeded");
                    } else if (result.isSuccessful()) {
                        log(INFO, "Index update succeeded");
                    }
                } finally {
                    fetcher.disconnect();
                }
            }
        }

        @Nonnull
        @Override
        protected HBox createGraphic() {
            return new HBox(FontIcons.glyphIcon("O_REPO", 16));
        }

        private class ArtifactListener implements ArtifactScanningListener {

            private final Queue<ArtifactContext> artifactInfos = new ConcurrentLinkedQueue<>();

            @Override
            public void scanningStarted(IndexingContext ctx) {
                updateMessage(m("Start scanning"));
            }

            @Override
            public void scanningFinished(IndexingContext ctx, ScanningResult result) {
                updateMessage(m("Scanning finished"));
            }

            @Override
            public void artifactError(ArtifactContext ac, Exception e) {
                log(WARNING, "Artifact {0} error", e, ac.getArtifact());
            }

            @Override
            public void artifactDiscovered(ArtifactContext ac) {
                if (ac.getArtifactInfo() != null) {
                    artifactInfos.add(ac);
                }
            }
        }

        private class WagonListener implements TransferListener {

            private final ConcurrentHashMap<String, AtomicLong> progressMap = new ConcurrentHashMap<>();

            @Override
            public void transferInitiated(TransferEvent transferEvent) {
                updateMessage(m("Initiated transferring {0}", transferEvent.getResource().getName()));
            }

            @Override
            public void transferStarted(TransferEvent transferEvent) {
                updateMessage(m("Started transferring {0}", transferEvent.getResource().getName()));
                updateProgress(0, transferEvent.getResource().getContentLength());
            }

            @Override
            public void transferProgress(TransferEvent transferEvent, byte[] buffer, int length) {
                final long count = progressMap
                        .computeIfAbsent(transferEvent.getResource().getName(), k -> new AtomicLong())
                        .addAndGet(length);
                updateProgress(count, transferEvent.getResource().getContentLength());
                updateMessage(m("Transferring {0} {1}/{2}",
                        transferEvent.getResource().getName(),
                        count,
                        transferEvent.getResource().getContentLength()));
            }

            @Override
            public void transferCompleted(TransferEvent transferEvent) {
                updateProgress(0, transferEvent.getResource().getContentLength());
                updateMessage(m("Completed {0}", transferEvent.getResource().getName()));
            }

            @Override
            public void transferError(TransferEvent transferEvent) {
                n(WARNING, "Unable to transfer {0}",
                        transferEvent.getException(), transferEvent.getResource().getName());
            }

            @Override
            public void debug(String message) {
                log(FINE, "{0}", message);
            }
        }
    }
}
