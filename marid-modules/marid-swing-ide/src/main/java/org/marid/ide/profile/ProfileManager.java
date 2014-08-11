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

package org.marid.ide.profile;

import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;

import javax.swing.event.EventListenerList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EventListener;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProfileManager implements LogSupport, SysPrefSupport {

    protected final ConcurrentSkipListMap<String, Profile> profileMap = new ConcurrentSkipListMap<>();
    protected final EventListenerList listenerList = new EventListenerList();

    public ProfileManager() {
        final Path dir = getSysPref(Path.class, "profilesDir", defaultPath());
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
    }
}
