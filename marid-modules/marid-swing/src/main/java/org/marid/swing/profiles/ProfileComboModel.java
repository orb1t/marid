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

package org.marid.swing.profiles;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProfileComboModel extends AbstractListModel<String> implements ComboBoxModel<String>, NodeChangeListener {

    private final Preferences preferences;
    private final Set<String> profileSet;
    private String selectedItem;

    public ProfileComboModel(Preferences preferences) {
        this.preferences = preferences.node("profiles");
        try {
            this.preferences.addNodeChangeListener(this);
            this.profileSet = new TreeSet<>(Arrays.asList(preferences.childrenNames()));
        } catch (BackingStoreException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem == null ? null : anItem.toString();
    }

    @Override
    public String getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return profileSet.size();
    }

    @Override
    public String getElementAt(int index) {
        int i = 0;
        for (final String profile : profileSet) {
            if (i == index) {
                return profile;
            }
            i++;
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        preferences.removeNodeChangeListener(this);
    }

    @Override
    public void childAdded(final NodeChangeEvent evt) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                profileSet.add(evt.getChild().name());
                int index = -1, i = 0;
                for (final String profile : profileSet) {
                    if (profile.equals(evt.getChild().name())) {
                        index = i;
                        break;
                    }
                    i++;
                }
                if (index >= 0) {
                    fireIntervalRemoved(this, index, index);
                }
            }
        });
    }

    @Override
    public void childRemoved(final NodeChangeEvent evt) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                int index = -1, i = 0;
                for (final String profile : profileSet) {
                    if (profile.equals(evt.getChild().name())) {
                        index = i;
                        break;
                    }
                    i++;
                }
                if (index >= 0) {
                    profileSet.remove(evt.getChild().name());
                    fireIntervalRemoved(this, index, index);
                }
            }
        });
    }
}
