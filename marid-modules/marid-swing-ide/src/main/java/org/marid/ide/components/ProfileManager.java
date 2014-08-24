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

import javax.swing.event.EventListenerList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProfileManager implements LogSupport, SysPrefSupport {

    protected final ConcurrentSkipListMap<String, Profile> profileMap = new ConcurrentSkipListMap<>();
    protected final EventListenerList listenerList = new EventListenerList();

    public ProfileManager() {
        final Path profilesDir = getProfilesDir();
        try (final Stream<Path> stream = Files.walk(profilesDir, 1)) {
            stream
                    .filter(Files::isDirectory)
                    .filter(p -> !profilesDir.equals(p))
                    .map(Profile::new)
                    .forEach(p -> profileMap.put(p.getName(), p));
        } catch (Exception x) {
            warning("Unable to walk {0}", x, profilesDir);
        }
    }

    public void addProfileEventListener(ProfileEventListener profileEventListener) {
        listenerList.add(ProfileEventListener.class, profileEventListener);
    }

    public void removeProfileEventListener(ProfileEventListener profileEventListener) {
        listenerList.remove(ProfileEventListener.class, profileEventListener);
    }

    protected Path defaultPath() {
        return Paths.get(System.getProperty("user.home"), "marid", "profiles");
    }

    public Path getProfilesDir() {
        try {
            final Path path = getSysPref("profilesDir", defaultPath());
            if (!Files.isDirectory(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception x) {
            warning("Unable to get profiles directory", x);
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    public void addProfile(Profile profile) {
        profileMap.put(profile.getName(), profile);
        for (final ProfileEventListener profileEventListener : listenerList.getListeners(ProfileEventListener.class)) {
            profileEventListener.profileAdded(profile);
        }
    }

    public void removeProfile(String name) {
        final Profile profile = profileMap.remove(name);
        if (profile != null) {
            try (final Profile p = profile) {
                for (final ProfileEventListener eventListener : listenerList.getListeners(ProfileEventListener.class)) {
                    eventListener.profileRemoved(p);
                }
            } catch (Exception x) {
                warning("Unable to close profile {0}", x, name);
            }
        }
    }

    public List<Profile> getProfiles() {
        return new ArrayList<>(profileMap.values());
    }

    public void removeProfile(Profile profile) {
        removeProfile(profile.getName());
    }

    public void updateProfile(Profile profile) {
        removeProfile(profile.getName());
        addProfile(profile);
    }

    public interface ProfileEventListener extends EventListener {

        void profileAdded(Profile profile);

        void profileRemoved(Profile profile);

        void update();
    }
}
