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
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.DebugSettings;
import org.marid.ide.settings.JavaSettings;
import org.marid.io.ProcessManager;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.jfx.track.Tracks;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectRunnerPane extends BorderPane implements L10nSupport, LogSupport {

    final ProjectProfile profile;
    final ListView<String> out = listView();
    final ListView<String> err = listView();
    final Process process;
    final ProcessManager processManager;
    final PrintStream printStream;

    @Inject
    public ProjectRunnerPane(ProjectManager projectManager,
                             JavaSettings javaSettings,
                             DebugSettings debugSettings) throws IOException {
        profile = projectManager.getProfile();
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
        process = process(profile, javaSettings, debugSettings);
        printStream = new PrintStream(process.getOutputStream(), true);
        processManager = new ProcessManager(profile.getName(), process, consumer(out), consumer(err));
        final Thread watchThread = new Thread(null, () -> {
            try {
                final int result = process.waitFor();
                log(INFO, "[{0}] exited with code {1}", profile, result);
                Platform.runLater(() -> {
                    final ToolBar toolBar = (ToolBar) getTop();
                    toolBar.getItems().forEach(e -> e.setDisable(true));
                });
            } catch (InterruptedException x) {
                log(WARNING, "Interrupted");
            }
        }, "watchThread", 96L * 1024L);
        watchThread.start();
        setTop(new ToolbarBuilder()
                .add("Exit", FontIcon.M_STOP, e -> printStream.println("exit"))
                .addSeparator()
                .add("Dump", FontIcon.M_LIST, e -> printStream.println("dump"))
                .build());
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

    private static Process process(ProjectProfile profile,
                                   JavaSettings javaSettings,
                                   DebugSettings debugSettings) throws IOException {
        final List<String> args = new ArrayList<>();
        args.add(javaSettings.getJavaExecutable());
        Collections.addAll(args, javaSettings.getJavaArguments());
        if (debugSettings.isDebug()) {
            final char suspend = debugSettings.isSuspend() ? 'y' : 'n';
            final int port = debugSettings.getPort();
            args.add(format("-Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%d", suspend, port));
        }
        args.add("-jar");
        args.add(format("%s-%s.jar", profile.getModel().getArtifactId(), profile.getModel().getVersion()));
        final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(profile.getTarget().toFile());
        Log.log(INFO, "Running {0} in {1}", String.join(" ", processBuilder.command()), processBuilder.directory());
        return processBuilder.start();
    }

    public ProjectProfile getProfile() {
        return profile;
    }
}
