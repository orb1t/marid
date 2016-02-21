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

package org.marid.ide.project;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.marid.logging.LogSupport;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectMavenTransferListener implements TransferListener, LogSupport {

    private final ProjectProfile profile;

    public ProjectMavenTransferListener(ProjectProfile profile) {
        this.profile = profile;
    }

    @Nonnull
    @Override
    public Logger logger() {
        return profile.logger();
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        log(WARNING, "{0}", event);
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferFailed(TransferEvent event) {
        log(SEVERE, "{0}", event);
    }
}
