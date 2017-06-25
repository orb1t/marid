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

package org.marid.ide.service;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.maven.cli.MaridTransferEvent;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.eclipse.aether.transfer.TransferEvent;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.logging.IdeMavenLogHandler;
import org.marid.ide.panes.main.IdeStatusBar;
import org.marid.ide.project.ProjectMavenBuilder;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.status.IdeService;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.logging.LogComponent;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

import javax.annotation.Nonnull;
import java.util.ListIterator;
import java.util.logging.Logger;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectBuilderService extends IdeService<HBox> {

    private final IdeLogHandler logHandler;
    private final IdeStatusBar statusBar;
    private final ObjectFactory<ProjectMavenBuilder> builder;
    private final ApplicationEventMulticaster multicaster;

    private ProjectProfile profile;

    @Autowired
    public ProjectBuilderService(IdeLogHandler logHandler,
                                 IdeStatusBar statusBar,
                                 ObjectFactory<ProjectMavenBuilder> builder,
                                 ApplicationEventMulticaster multicaster) {
        this.logHandler = logHandler;
        this.statusBar = statusBar;
        this.builder = builder;
        this.multicaster = multicaster;
    }

    public ProjectBuilderService setProfile(ProjectProfile profile) {
        this.profile = profile;
        setOnRunning(event -> profile.enabledProperty().set(false));
        setOnFailed(event -> profile.enabledProperty().set(true));
        setOnSucceeded(event -> profile.enabledProperty().set(true));
        return this;
    }

    @Override
    protected BuilderTask createTask() {
        return new BuilderTask();
    }

    private class BuilderTask extends IdeTask {

        private ApplicationListener<MaridTransferEvent> transferEventListener;
        private ObservableList<TransferEvent> events;
        ListView<TransferEvent> view;

        private BuilderTask() {
            updateTitle(profile.getName());
        }

        @Override
        protected void execute() throws Exception {
            final ProjectMavenBuilder projectBuilder = builder.getObject()
                    .profile(profile)
                    .goals("clean", "install")
                    .profiles("conf");
            final int threadId = logHandler.registerBlockedThreadId();
            final IdeMavenLogHandler mavenLogHandler = new IdeMavenLogHandler(threadId);
            final Logger root = Logger.getLogger("");
            root.addHandler(mavenLogHandler);
            updateGraphic(box -> {
                final LogComponent logComponent = new LogComponent(mavenLogHandler.records);
                final BorderPane pane = new BorderPane(logComponent);
                BorderPane.setMargin(logComponent, new Insets(5));
                logComponent.setPrefSize(800, 600);
                statusBar.addNotification(Bindings.format("%s: %s", profile.getName(), ls("Maven Build")), pane);
                multicaster.addApplicationListener(transferEventListener = new TransferListener());
            });
            try {
                projectBuilder.build(result -> {
                    if (!result.exceptions.isEmpty()) {
                        final IllegalStateException thrown = new IllegalStateException("Maven build error");
                        result.exceptions.forEach(thrown::addSuppressed);
                        throw thrown;
                    }
                });
            } finally {
                logHandler.unregisterBlockedThreadId(threadId);
                root.removeHandler(mavenLogHandler);
                if (transferEventListener != null) {
                    multicaster.removeApplicationListener(transferEventListener);
                }
            }
        }

        @Nonnull
        @Override
        protected HBox createGraphic() {
            return new HBox(IdeShapes.circle(profile.hashCode(), 16));
        }

        @Override
        protected ContextMenu contextMenu() {
            return new ContextMenu();
        }

        private class TransferListener implements ApplicationListener<MaridTransferEvent> {

            @Override
            public void onApplicationEvent(MaridTransferEvent event) {
                if (event.getSource() != profile) {
                    return;
                }
                Platform.runLater(() -> {
                    if (events == null) {
                        events = FXCollections.observableArrayList();

                        view = new ListView<>(events);
                        view.setPrefSize(400, 800);
                        view.setCellFactory(param -> new ListCell<TransferEvent>() {
                            @Override
                            protected void updateItem(TransferEvent item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    setText(item.getResource().getFile().getName());
                                    switch (item.getType()) {
                                        case STARTED:
                                            setGraphic(FontIcons.glyphIcon("D_PLAY"));
                                            break;
                                        case CORRUPTED:
                                            setGraphic(FontIcons.glyphIcon("M_BUG_REPORT"));
                                            break;
                                        case FAILED:
                                            setGraphic(FontIcons.glyphIcon("M_SMS_FAILED"));
                                            break;
                                        case INITIATED:
                                            setGraphic(FontIcons.glyphIcon("M_INSERT_INVITATION"));
                                            break;
                                        case PROGRESSED:
                                            setGraphic(FontIcons.glyphIcon("D_MESSAGE_PROCESSING"));
                                            break;
                                        case SUCCEEDED:
                                            setGraphic(FontIcons.glyphIcon("F_STOP"));
                                            break;
                                        default:
                                            setGraphic(null);
                                            break;
                                    }
                                }
                            }
                        });

                        final PopOver popOver = new PopOver(view);
                        popOver.setArrowLocation(ArrowLocation.BOTTOM_LEFT);

                        addEventHandler(WorkerStateEvent.ANY, e -> {
                            if (DONE_EVENT_TYPES.contains(e.getEventType())) {
                                events.clear();
                                popOver.hide();
                            }
                        });

                        popOver.show(button);
                    }

                    for (final ListIterator<TransferEvent> i = events.listIterator(); i.hasNext(); ) {
                        final TransferEvent e = i.next();
                        if (e.getResource() == event.getEvent().getResource()) {
                            i.set(e);
                            return;
                        }
                    }
                    events.add(event.getEvent());
                    view.scrollTo(events.size() - 1);
                });
            }
        }
    }
}
