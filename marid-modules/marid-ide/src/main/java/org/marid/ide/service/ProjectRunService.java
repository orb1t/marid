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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.maven.model.Model;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.JavaSettings;
import org.marid.ide.status.IdeService;
import org.marid.io.ProcessManager;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.jfx.track.Tracks;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.project.ProjectFileType.TARGET;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectRunService extends IdeService<HBox> {

	private final JavaSettings javaSettings;

	private ProjectProfile profile;

	@Autowired
	public ProjectRunService(JavaSettings javaSettings) {
		this.javaSettings = javaSettings;
	}

	public ProjectRunService setProfile(ProjectProfile profile) {
		this.profile = profile;
		return this;
	}

	@Override
	protected IdeTask createTask() {
		return new RunTask();
	}

	private class RunTask extends IdeTask {

		private PrintStream printStream;

		@Override
		protected void execute() throws Exception {
			updateTitle(s("Running %s", profile.getName()));
			try (final ProjectRunnerPane pane = new ProjectRunnerPane()) {
				printStream = pane.printStream;
				pane.setPrefSize(1000, 800);
				updateGraphic(box -> {
					details.set(pane);
					box.setAlignment(Pos.CENTER_LEFT);
				});
				pane.process.waitFor();
			}
		}

		@Nonnull
		@Override
		protected HBox createGraphic() {
			return new HBox(5,
					IdeShapes.circle(profile.hashCode(), 20),
					build(new Button(null, glyphIcon("D_CLOSE")), b -> b.setOnAction(event -> {
						printStream.println("close");
						event.consume();
					})),
					build(new Button(null, glyphIcon("D_STOP")), b -> b.setOnAction(event -> {
						printStream.println("exit");
						event.consume();
					}))
			);
		}

		@Override
		protected ContextMenu contextMenu() {
			return new ContextMenu();
		}
	}

	class ProjectRunnerPane extends BorderPane implements AutoCloseable {

		private final ListView<String> out = listView();
		private final ListView<String> err = listView();
		private final Process process;
		private final ProcessManager processManager;
		private final PrintStream printStream;

		ProjectRunnerPane() {
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
					.add("Close", "M_CLOSE", e -> printStream.println("close"))
					.add("Exit", "M_STOP", e -> printStream.println("exit"))
					.addSeparator()
					.add("Dump", "M_LIST", e -> printStream.println("dump"))
					.build());
			setCenter(tabPane);
		}

		private Consumer<String> consumer(ListView<String> listView) {
			return line -> Platform.runLater(() -> listView.getItems().add(line));
		}

		private ListView<String> listView() {
			final ListView<String> listView = new ListView<>();
			listView.setStyle("-fx-font-family: monospace; -fx-font-size: small");
			Tracks.track(listView, listView.getItems(), listView.getSelectionModel());
			return listView;
		}

		private Process process(JavaSettings javaSettings) {
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
			args.add(String.format("%s-%s.jar", profile.getModel().getArtifactId(), profile.getModel().getVersion()));
			final ProcessBuilder processBuilder = new ProcessBuilder(args).directory(profile.get(TARGET).toFile());
			log(INFO, "Running {0} in {1}", String.join(" ", processBuilder.command()), processBuilder.directory());
			try {
				return processBuilder.start();
			} catch (IOException x) {
				throw new UncheckedIOException(x);
			}
		}

		public ProjectProfile getProfile() {
			return profile;
		}

		@Override
		public void close() throws Exception {
			processManager.close();
		}
	}
}
