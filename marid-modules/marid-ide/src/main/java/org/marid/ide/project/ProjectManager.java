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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.maven.model.merge.MavenModelMerger;
import org.apache.maven.model.merge.ModelMerger;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.project.editors.ProjectDialog;
import org.marid.ide.project.runner.ProjectRunner;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Provider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.util.Utils.callWithTime;

/**
 * @author Dmitry Ovchinnikov
 */
@ApplicationScoped
public class ProjectManager implements PrefSupport, LogSupport {

    private final ModelMerger modelMerger = new MavenModelMerger();
    private final ObjectProperty<ProjectProfile> profile = new SimpleObjectProperty<>();
    private final ObservableList<ProjectProfile> profiles = FXCollections.observableArrayList();

    public ProjectManager() {
        profile.set(new ProjectProfile(getPref("profile", "default")));
        if (!isPresent()) {
            profile.set(new ProjectProfile("default"));
        }
        final Path profilesDir = getProfile().getPath().getParent();
        try (final Stream<Path> stream = Files.list(profilesDir)) {
            stream
                    .filter(p -> Files.isDirectory(p) && !profilesDir.equals(p))
                    .map(p -> new ProjectProfile(p.getFileName().toString()))
                    .forEach(profiles::add);
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate profiles", x);
        }
        if (!profiles.contains(getProfile())) {
            profiles.add(getProfile());
        }
        profiles.sort(Comparator.comparing(ProjectProfile::getName));
    }

    @PreDestroy
    private void savePrefs() {
        putPref("profile", getProfile().getName());
    }

    boolean isPresent() {
        return Files.isDirectory(getProfile().getPath());
    }

    @Produces
    @Dependent
    public ProjectProfile getProfile() {
        return profile.get();
    }

    public ObjectProperty<ProjectProfile> profileProperty() {
        return profile;
    }

    public ObservableList<ProjectProfile> getProfiles() {
        return profiles;
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Project setup...", group = "setup", icon = O_TOOLS)
    @IdeToolbarItem(group = "projectSetup")
    public EventHandler<ActionEvent> projectSetup(Provider<ProjectDialog> editorProvider,
                                                  Provider<IdeModelMerger> ideModelMergerProvider) {
        return event -> {
            final ProjectDialog editor = editorProvider.get();
            editor.showAndWait().ifPresent(model -> {
                modelMerger.merge(getProfile().getModel(), model, true, null);
                final IdeModelMerger ideModelMerger = ideModelMergerProvider.get();
                ideModelMerger.merge(getProfile().getModel(), model);
            });
        };
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Save", group = "io", icon = F_SAVE, key = "Ctrl+S")
    @IdeToolbarItem(group = "projectIO")
    public EventHandler<ActionEvent> projectSave(Provider<ProjectSaver> projectSaverProvider) {
        return event -> callWithTime(
                () -> projectSaverProvider.get().save(),
                time -> log(INFO, "Profile [{0}] saved in {1} ms", profile, time));
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Build", group = "pb", icon = D_CLOCK_FAST, key = "F9")
    @IdeToolbarItem(group = "projectBuild")
    public EventHandler<ActionEvent> projectBuild(Provider<ProjectProfile> profileProvider) {
        return event -> {
            final ProjectProfile profile = profileProvider.get();
            final MavenProjectBuilder mavenProjectBuilder = new MavenProjectBuilder(profile)
                    .goals("clean", "install");
            try {
                mavenProjectBuilder.build();
            } catch (Exception x) {
                log(WARNING, "Unable to build", x);
            }
        };
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Run", group = "pb", icon = F_PLAY, key = "F5")
    @IdeToolbarItem(group = "projectBuild")
    public EventHandler<ActionEvent> projectRun(Provider<ProjectRunner> projectRunnerProvider) {
        return event -> {
            final ProjectRunner projectRunner = projectRunnerProvider.get();
            projectRunner.show();
        };
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
