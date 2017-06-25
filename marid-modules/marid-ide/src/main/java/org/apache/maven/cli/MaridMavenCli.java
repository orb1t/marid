package org.apache.maven.cli;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.marid.ide.project.ProjectProfile;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridMavenCli extends MavenCli {

    private final ApplicationEventPublisher eventPublisher;
    private final ProjectProfile profile;

    public MaridMavenCli(ClassWorld classWorld, ApplicationEventPublisher eventPublisher, ProjectProfile profile) {
        super(classWorld);
        this.eventPublisher = eventPublisher;
        this.profile = profile;
    }

    @Override
    protected TransferListener getConsoleTransferListener(boolean printResourceNames) {
        return new MaridMavenTransferListener();
    }

    @Override
    protected TransferListener getBatchTransferListener() {
        return new MaridMavenTransferListener();
    }

    public class MaridMavenTransferListener implements TransferListener {

        @Override
        public void transferInitiated(TransferEvent transferEvent) throws TransferCancelledException {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }

        @Override
        public void transferStarted(TransferEvent transferEvent) throws TransferCancelledException {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }

        @Override
        public void transferProgressed(TransferEvent transferEvent) throws TransferCancelledException {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }

        @Override
        public void transferCorrupted(TransferEvent transferEvent) throws TransferCancelledException {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }

        @Override
        public void transferSucceeded(TransferEvent transferEvent) {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }

        @Override
        public void transferFailed(TransferEvent transferEvent) {
            eventPublisher.publishEvent(new MaridTransferEvent(profile, transferEvent));
        }
    }
}
