/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.ide.components;

import org.marid.ide.profile.Profile;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProfileManager implements LogSupport, SysPrefSupport {

    protected final ConcurrentSkipListMap<String, Profile> profileMap = new ConcurrentSkipListMap<>();
    protected final Map<Object, Consumer<Profile>> addProfileConsumers = synchronizedMap(new WeakHashMap<>());
    protected final Map<Object, Consumer<Profile>> removeProfileConsumers = synchronizedMap(new WeakHashMap<>());

    public ProfileManager() throws IOException {
        final Path profilesDir = getProfilesDir();
        try (final Stream<Path> stream = Files.list(profilesDir)) {
            stream.filter(Files::isDirectory).map(Profile::new).forEach(p -> profileMap.put(p.getName(), p));
        }
    }

    public void addProfileAddConsumer(Object gcBase, Consumer<Profile> consumer) {
        addProfileConsumers.put(gcBase, consumer);
    }

    public void addProfileRemoveConsumer(Object gcBase, Consumer<Profile> consumer) {
        removeProfileConsumers.put(gcBase, consumer);
    }

    protected Path defaultPath() {
        return Paths.get(System.getProperty("user.home"), "marid", "profiles");
    }

    public Path getProfilesDir() {
        final Path path = getSysPref("profilesDir", defaultPath());
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException x) {
                throw new IllegalStateException(x);
            }
        }
        return path;
    }

    public Profile addProfile(String name) {
        final Profile profile = new Profile(getProfilesDir().resolve(name));
        addProfileConsumers.forEach((k, v) -> v.accept(profile));
        return profile;
    }

    public void removeProfile(String name) {
        try (final Profile profile = profileMap.remove(name)) {
            removeProfileConsumers.forEach((k, v) -> v.accept(profile));
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    public Profile getProfileByName(String name) {
        return profileMap.get(name);
    }

    public Profile getCurrentProfile() {
        return profileMap.get(getSysPref("currentProfile", "default"));
    }

    public void setCurrentProfile(Profile profile) {
        if (profile == null) {
            SYSPREFS.remove("currentProfile");
        } else {
            putSysPref("currentProfile", profile.getName());
        }
    }

    public List<Profile> getProfiles() {
        return new ArrayList<>(profileMap.values());
    }
}
