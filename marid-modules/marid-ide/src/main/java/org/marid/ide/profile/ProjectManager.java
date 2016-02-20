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

package org.marid.ide.profile;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.maven.model.merge.MavenModelMerger;
import org.apache.maven.model.merge.ModelMerger;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.profile.editors.ProjectDataDialog;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.marid.util.Utils.callWithTime;

/**
 * @author Dmitry Ovchinnikov
 */
@ApplicationScoped
public class ProjectManager implements PrefSupport, LogSupport {

    private final ModelMerger modelMerger = new MavenModelMerger();
    private ProjectProfile profile;

    public ProjectManager() {
        profile = new ProjectProfile(getPref("profile", "default"));
        if (!isPresent()) {
            profile = new ProjectProfile("default");
        }
    }

    @PreDestroy
    private void save() {
        putPref("profile", profile.getName());
    }

    public boolean isPresent() {
        return Files.isDirectory(profile.getPath());
    }

    @Produces
    @Dependent
    public ProjectProfile getProfile() {
        return profile;
    }

    public void setProfile(ProjectProfile profile) {
        this.profile = profile;
    }

    public Set<ProjectProfile> getProfiles() {
        final Set<ProjectProfile> profiles = new LinkedHashSet<>();
        final Path profilesDir = profile.getPath().getParent();
        try (final Stream<Path> stream = Files.walk(profilesDir)) {
            stream
                    .filter(p -> Files.isDirectory(p) && !profilesDir.equals(p))
                    .map(p -> new ProjectProfile(p.getFileName().toString()))
                    .forEach(profiles::add);
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate profiles", x);
        }
        profiles.add(profile);
        return profiles;
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Project setup...", group = "ps", oIcons = {OctIcon.TOOLS})
    @IdeToolbarItem(group = "project")
    public EventHandler<ActionEvent> projectSetup(Provider<ProjectDataDialog> editorProvider) {
        return event -> {
            final ProjectDataDialog editor = editorProvider.get();
            editor.showAndWait().ifPresent(model -> {
                getProfile().getModel().setOrganization(null);
                modelMerger.merge(getProfile().getModel(), model, true, null);
            });
        };
    }

    @Produces
    @IdeMenuItem(menu = "Project", text = "Save", group = "ps", faIcons = {FontAwesomeIcon.SAVE}, key = "Ctrl+S")
    @IdeToolbarItem(group = "project")
    public EventHandler<ActionEvent> projectSave() {
        return event -> callWithTime(profile::save, time -> log(INFO, "Profile {0} saved in {1} ms", profile, time));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
