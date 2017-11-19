/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.ide.service;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectMavenBuilder;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.status.IdeService;
import org.marid.io.ProcessManager;
import org.marid.idelib.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static java.lang.System.lineSeparator;
import static javafx.application.Platform.runLater;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectBuilderService extends IdeService<HBox> {

  private final ObjectFactory<ProjectMavenBuilder> builder;
  private final TextArea out = new TextArea();
  private final TextArea err = new TextArea();

  private ProjectProfile profile;

  @Autowired
  public ProjectBuilderService(ObjectFactory<ProjectMavenBuilder> builder) {
    this.builder = builder;

    out.setFont(Font.font("Monospaced"));
    err.setFont(Font.font("Monospaced"));
  }

  public ProjectBuilderService setProfile(ProjectProfile profile) {
    this.profile = profile;
    setOnRunning(event -> profile.enabledProperty().set(false));
    setOnFailed(event -> profile.enabledProperty().set(true));
    setOnSucceeded(event -> profile.enabledProperty().set(true));
    return this;
  }

  @Override
  protected IdeTask createTask() {
    return new BuilderTask();
  }

  private class BuilderTask extends IdeTask {

    private BuilderTask() {
      updateTitle(profile.getName() + ": " + s("Maven Build"));
    }

    @Override
    protected void execute() throws Exception {
      final ProjectMavenBuilder projectBuilder = builder.getObject()
          .profile(profile)
          .goals("clean", "install");
      updateGraphic(box -> {
        final Tab outTab = new Tab("Out", out);
        final Tab errTab = new Tab("Err", err);
        final TabPane tabPane = new TabPane(outTab, errTab);
        tabPane.setPrefSize(800, 800);
        details.set(tabPane);
      });
      final Consumer<String> o = l -> runLater(() -> out.appendText(l + lineSeparator()));
      final Consumer<String> e = l -> runLater(() -> err.appendText(l + lineSeparator()));
      final ProcessManager manager = projectBuilder.build(o, e);

      manager.waitFor();
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
  }
}
