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

package org.marid.ide.project;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.ide.IdePrefs;
import org.marid.ide.common.Directories;
import org.marid.ide.event.FileAddedEvent;
import org.marid.ide.event.FileRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    private final Directories directories;
    private final ObjectProperty<ProjectProfile> profile = new SimpleObjectProperty<>();
    private final ObservableList<ProjectProfile> profiles = FXCollections.observableArrayList();

    @Autowired
    public ProjectManager(Directories directories) throws IOException {
        try (final Stream<Path> stream = Files.list((this.directories = directories).getProfiles())) {
            stream.map(p -> new ProjectProfile(p.getParent(), p.getFileName().toString())).forEach(profiles::add);
        }
        profiles.sort(Comparator.comparing(ProjectProfile::getName));
    }

    @PostConstruct
    private void init() {
        final String profileName = IdePrefs.PREFERENCES.get("profile", null);
        profiles.stream().filter(p -> p.getName().equals(profileName)).findAny().ifPresent(profile::set);
        if (profile.get() == null && !profiles.isEmpty()) {
            profile.set(profiles.get(0));
        }
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

    private ProjectProfile add(String name) {
        final ProjectProfile profile = profiles.stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElseGet(() -> new ProjectProfile(directories.getProfiles(), name));
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
