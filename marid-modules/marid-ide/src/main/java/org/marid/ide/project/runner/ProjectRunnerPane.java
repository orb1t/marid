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

package org.marid.ide.project.runner;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.JavaSettings;
import org.marid.io.ProcessManager;
import org.marid.jfx.track.Tracks;
import org.marid.l10n.L10nSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectRunnerPane extends BorderPane implements L10nSupport {

    final ProjectProfile profile;
    final ListView<String> out = listView();
    final ListView<String> err = listView();
    final Process process;
    final ProcessManager processManager;
    final PrintStream printStream;

    @Inject
    public ProjectRunnerPane(ProjectProfile profile, JavaSettings javaSettings) throws IOException {
        this.profile = profile;
        final ScrollPane outPane = new ScrollPane(out);
        final ScrollPane errPane = new ScrollPane(err);
        Arrays.asList(outPane, errPane).forEach(pane -> {
            pane.setFitToHeight(true);
            pane.setFitToWidth(true);
        });
        final TabPane tabPane = new TabPane(
                new Tab(s("Standard output"), outPane),
                new Tab(s("Error output"), errPane));
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.setFocusTraversable(false);
        process = process(profile, javaSettings);
        printStream = new PrintStream(process.getOutputStream(), true);
        processManager = new ProcessManager(profile.getName(), process, consumer(out), consumer(err));
        setTop(new ProjectRunnerToolbar(this));
        setCenter(tabPane);
    }

    private Consumer<String> consumer(ListView<String> listView) {
        return line -> Platform.runLater(() -> listView.getItems().add(line));
    }

    private static ListView<String> listView() {
        final ListView<String> listView = new ListView<>();
        listView.setStyle("-fx-font-family: monospace; -fx-font-size: small");
        Tracks.track(listView, listView.getItems(), listView.getSelectionModel());
        return listView;
    }

    private static Process process(ProjectProfile profile, JavaSettings javaSettings) throws IOException {
        final List<String> args = new ArrayList<>();
        args.add(javaSettings.getJavaExecutable());
        Collections.addAll(args, javaSettings.getJavaArguments());
        args.add("-jar");
        args.add(String.format("%s-%s.jar", profile.getModel().getArtifactId(), profile.getModel().getVersion()));
        return new ProcessBuilder(args)
                .directory(profile.getTarget().toFile())
                .start();
    }

    public ProjectProfile getProfile() {
        return profile;
    }
}
