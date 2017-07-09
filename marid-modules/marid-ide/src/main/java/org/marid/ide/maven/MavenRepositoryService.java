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

import javafx.scene.control.ContextMenu;
import javafx.scene.layout.HBox;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.WagonHelper.WagonFetcher;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.marid.ide.status.IdeService;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
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

    @Autowired
    public MavenRepositoryService(MavenRepositoryManager manager) {
        this.manager = manager;

        setOnRunning(event -> manager.updating.set(true));
        setOnCancelled(event -> manager.updating.set(false));
        setOnFailed(event -> manager.updating.set(false));
        setOnSucceeded(event -> manager.updating.set(false));
    }

    @Override
    protected IdeTask createTask() {
        return new RepositoryUpdateTask();
    }

    public class RepositoryUpdateTask extends IdeTask {

        final WagonFetcher fetcher = new WagonFetcher(manager.wagon, new WagonListener(), null, null);

        @Override
        protected void execute() throws Exception {
            try {
                updateTitle(s("Repository Update"));
                final IndexUpdateRequest request = new IndexUpdateRequest(manager.context, fetcher);
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

        @Override
        protected void cancelled() {
            try {
                log(INFO, "Disconnecting");
                fetcher.disconnect();
            } catch (IOException x) {
                log(WARNING, "Unable to disconnect", x);
            }
        }

        @Nonnull
        @Override
        protected HBox createGraphic() {
            return new HBox(FontIcons.glyphIcon("O_REPO", 16));
        }

        @Override
        protected ContextMenu contextMenu() {
            return new ContextMenu();
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
