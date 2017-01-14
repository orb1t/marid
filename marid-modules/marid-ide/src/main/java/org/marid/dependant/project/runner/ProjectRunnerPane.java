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

package org.marid.dependant.project.runner;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Model;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.JavaSettings;
import org.marid.io.ProcessManager;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.jfx.track.Tracks;
import org.marid.l10n.L10n;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectRunnerPane extends BorderPane implements LogSupport {

    final ProjectProfile profile;
    final ListView<String> out = listView();
    final ListView<String> err = listView();
    final Process process;
    final ProcessManager processManager;
    final PrintStream printStream;

    @Autowired
    public ProjectRunnerPane(ProjectProfile profile, JavaSettings javaSettings) throws IOException {
        this.profile = profile;
        final ScrollPane outPane = new ScrollPane(out);
        final ScrollPane errPane = new ScrollPane(err);
        Arrays.asList(outPane, errPane).forEach(pane -> {
            pane.setFitToHeight(true);
            pane.setFitToWidth(true);
        });
        final TabPane tabPane = new TabPane(
                new Tab(L10n.s("Standard output"), outPane),
                new Tab(L10n.s("Error output"), errPane));
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.setFocusTraversable(false);
        process = process(javaSettings);
        printStream = new PrintStream(process.getOutputStream(), true);
        processManager = new ProcessManager(profile.getName(), process, consumer(out), consumer(err), 65536, 1L, MINUTES);
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
                .add("Close", FontIcon.M_CLOSE, e -> printStream.println("close"))
                .add("Exit", FontIcon.M_STOP, e -> printStream.println("exit"))
                .addSeparator()
                .add("Dump", FontIcon.M_LIST, e -> printStream.println("dump"))
                .build());
        setCenter(tabPane);
    }

    @PreDestroy
    private void destroy() {
        process.destroyForcibly();
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

    private Process process(JavaSettings javaSettings) throws IOException {
        final List<String> args = new ArrayList<>();
        args.add(javaSettings.getJavaExecutable());
        Collections.addAll(args, javaSettings.getJavaArguments());
        final Model model = profile.getModel();
        final Properties properties = model.getProperties();
        final String debug = properties.getProperty("marid.debug", "false");
        if ("true".equals(debug)) {
            final String port = properties.getProperty("marid.debug.port", "5005");
            final String timeout = properties.getProperty("marid.debug.timeout", "30000");
            final String suspend = properties.getProperty("marid.debug.suspend", "n");
            final String server = properties.getProperty("marid.debug.server", "y");
            final Map<String, String> params = new LinkedHashMap<>();
            params.put("transport", "dt_socket");
            params.put("server", server);
            params.put("suspend", suspend);
            params.put("address", port);
            params.put("timeout", timeout);
            final String arg = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(",", "-agentlib:jdwp=", ""));
            args.add(arg);
        }
        args.add("-jar");
        args.add(format("%s-%s.jar", profile.getModel().getArtifactId(), profile.getModel().getVersion()));
        final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(profile.getTarget().toFile());
        log(INFO, "Running {0} in {1}", String.join(" ", processBuilder.command()), processBuilder.directory());
        return processBuilder.start();
    }

    public ProjectProfile getProfile() {
        return profile;
    }
}
