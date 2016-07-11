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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.IdePrefs;
import org.marid.logging.LogSupport;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.Collections.binarySearch;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectManager implements LogSupport {

    private final ObjectProperty<ProjectProfile> profile = new SimpleObjectProperty<>();
    private final ObservableList<ProjectProfile> profiles = FXCollections.observableArrayList();

    public ProjectManager() {
        profile.set(new ProjectProfile(IdePrefs.PREFERENCES.get("profile", "default")));
        if (!isPresent()) {
            profile.set(new ProjectProfile("default"));
        }
        profiles.add(profile.get());
        final Path profilesDir = getProfile().getPath().getParent();
        try (final Stream<Path> stream = Files.list(profilesDir)) {
            stream
                    .filter(p -> Files.isDirectory(p) && !profilesDir.equals(p))
                    .filter(p -> !p.getFileName().toString().equals(profile.get().getName()))
                    .map(p -> new ProjectProfile(p.getFileName().toString()))
                    .forEach(profiles::add);
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate profiles", x);
        }
        profiles.sort(Comparator.comparing(ProjectProfile::getName));
    }

    @PreDestroy
    private void savePrefs() {
        IdePrefs.PREFERENCES.put("profile", getProfile().getName());
    }

    boolean isPresent() {
        return Files.isDirectory(getProfile().getPath());
    }

    public ProjectProfile getProfile() {
        return profile.get();
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
    }

    public static void onBeanNameChange(ProjectProfile profile, String oldName, String newName) {
        profile.getBeanFiles().values().forEach(beanFile -> {
            beanFile.beans.forEach(beanData -> {
                if (beanData.factoryBean.isEqualTo(oldName).get()) {
                    beanData.factoryBean.set(newName);
                }
                beanData.constructorArgs.forEach(constructorArg -> {
                    if (constructorArg.ref.isEqualTo(oldName).get()) {
                        constructorArg.ref.set(newName);
                    }
                });
                beanData.properties.forEach(property -> {
                    if (property.ref.isEqualTo(oldName).get()) {
                        property.ref.set(newName);
                    }
                });
            });
        });
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
