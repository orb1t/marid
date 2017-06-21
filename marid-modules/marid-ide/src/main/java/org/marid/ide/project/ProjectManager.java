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

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.IdePrefs;
import org.marid.ide.common.Directories;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.binarySearch;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectManager {

    private final ObjectProperty<ProjectProfile> profile = new SimpleObjectProperty<>();
    private final ObservableList<ProjectProfile> profiles = FXCollections.observableArrayList();

    @Autowired
    public ProjectManager(Directories directories) throws IOException {
        try (final Stream<Path> stream = Files.list(directories.getProfiles())) {
            stream.map(p -> new ProjectProfile(p.getFileName().toString())).forEach(profiles::add);
        }
        profiles.sort(Comparator.comparing(ProjectProfile::getName));
        final String profileName = IdePrefs.PREFERENCES.get("profile", null);
        profiles.stream().filter(p -> p.getName().equals(profileName)).findAny().ifPresent(profile::set);
    }

    @PreDestroy
    private void savePrefs() {
        IdePrefs.PREFERENCES.put("profile", getProfile().getName());
    }

    public ProjectProfile getProfile() {
        return profile.get();
    }

    public Optional<ProjectProfile> getProfile(Path path) {
        return profiles.stream().filter(p -> path.startsWith(p.getPath())).findFirst();
    }

    public ObjectProperty<ProjectProfile> profileProperty() {
        return profile;
    }

    public ObservableList<ProjectProfile> getProfiles() {
        return profiles;
    }

    public ProjectProfile add(String name) {
        final ProjectProfile profile = profiles.stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElseGet(() -> new ProjectProfile(name));
        if (profiles.contains(profile)) {
            return profile;
        }
        final int index = -(binarySearch(profiles, profile, Comparator.comparing(ProjectProfile::getName)) + 1);
        profiles.add(index, profile);
        return profile;
    }

    public void remove(ProjectProfile profile) {
        if (profiles.remove(profile)) {
            profile.delete();
        }
        if (this.profile.get() != null && this.profile.get().equals(profile)) {
            this.profile.set(null);
        }
    }

    @EventListener(condition = "T(java.nio.file.Files).isDirectory(#e.source) && #e.source.parent.equals(@directories.profiles)")
    public void onAdd(FileAddedEvent e) {
        Platform.runLater(() -> add(e.getSource().getFileName().toString()));
    }

    @EventListener(condition = "#e.source.parent.equals(@directories.profiles)")
    public void onRemove(FileRemovedEvent e) {
        profiles.stream()
                .filter(p -> p.getName().equals(e.getSource().getFileName().toString()))
                .findFirst()
                .ifPresent(p -> Platform.runLater(() -> remove(p)));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
