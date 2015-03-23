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

package org.marid.ide.gui;

import org.marid.ide.base.IdeStatusLine;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.profile.Profile;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.util.SysPropsSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static javax.swing.BorderFactory.createEtchedBorder;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("ideStatusLine")
public class IdeStatusLineImpl extends JPanel implements IdeStatusLine, SysPrefSupport, SysPropsSupport, LogSupport {

    protected final ProfileManager profileManager;
    protected final JLabel status = new JLabel("Done");
    protected final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    protected final JLabel timeLabel = new JLabel(currentTime());
    protected final ProfileManagerListModel profileListModel;
    protected final JComboBox<Profile> profilesCombo;

    @Autowired
    public IdeStatusLineImpl(ProfileManager profileManager) {
        setBorder(createEtchedBorder());
        setLayout(new GridBagLayout());
        this.profileManager = profileManager;
        this.profileListModel = new ProfileManagerListModel();
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.BASELINE;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 3, 2, 3);
        c.weightx = 1.0;
        add(status, c);
        c.weightx = 0.0;
        add(new JSeparator(SwingConstants.VERTICAL), c);
        add(profilesCombo = new JComboBox<>(profileListModel), c);
        add(new JSeparator(SwingConstants.VERTICAL), c);
        add(timeLabel, c);
    }

    @Autowired
    private void prepare(IdeTimer ideTimer) {
        ideTimer.addListener(e -> timeLabel.setText(currentTime()));
    }

    private String currentTime() {
        return dateFormat.format(new Date());
    }

    protected class ProfileManagerListModel extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {

        protected final List<Profile> profiles;

        public ProfileManagerListModel() {
            profiles = profileManager.getProfiles();
            profileManager.addProfileAddConsumer(this, p -> {
                profiles.clear();
                profiles.addAll(profileManager.getProfiles());
                final int index = profiles.indexOf(p);
                if (index >= 0) {
                    fireIntervalAdded(this, index, index);
                }
            });
            profileManager.addProfileRemoveConsumer(this, p -> {
                final int index = profiles.indexOf(p);
                if (index >= 0) {
                    profiles.remove(p);
                    fireIntervalRemoved(this, index, index);
                }
            });
        }

        @Override
        public void setSelectedItem(Object anItem) {
            profileManager.setCurrentProfile((Profile) anItem);
        }

        @Override
        public Profile getSelectedItem() {
            return profileManager.getCurrentProfile();
        }

        @Override
        public int getSize() {
            return profiles.size();
        }

        @Override
        public Profile getElementAt(int index) {
            return profiles.get(index);
        }

        public void update() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
