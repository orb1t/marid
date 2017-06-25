package org.apache.maven.cli;

import org.eclipse.aether.transfer.TransferEvent;
import org.marid.ide.project.ProjectProfile;
import org.springframework.context.ApplicationEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTransferEvent extends ApplicationEvent {

    private final TransferEvent event;

    public MaridTransferEvent(ProjectProfile profile, TransferEvent event) {
        super(profile);
        this.event = event;
    }

    public TransferEvent getEvent() {
        return event;
    }

    @Override
    public ProjectProfile getSource() {
        return (ProjectProfile) super.getSource();
    }
}
