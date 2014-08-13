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

package org.marid.ide;

import org.marid.ide.profile.Profile;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.util.SysPropsSupport;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeStatusLine extends JPanel implements SysPrefSupport, SysPropsSupport, LogSupport {

    protected final IdeFrame ideFrame;
    protected final JLabel status = new JLabel("Done");
    protected final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    protected final JLabel timeLabel = new JLabel(currentTime());
    protected final ProfileListModel profileListModel = new ProfileListModel();
    protected final JComboBox<Profile> profilesCombo = new JComboBox<>(profileListModel);

    public IdeStatusLine(IdeFrame ideFrame) {
        setLayout(new GridBagLayout());
        this.ideFrame = ideFrame;
        final GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.BASELINE;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 3, 2, 3);
        c.weightx = 1.0;
        add(status, c);
        c.weightx = 0.0;
        add(new JSeparator(SwingConstants.VERTICAL), c);
        add(profilesCombo, c);
        add(new JSeparator(SwingConstants.VERTICAL), c);
        add(timeLabel, c);
    }

    private String currentTime() {
        return dateFormat.format(new Date());
    }

    protected class ProfileListModel extends AbstractListModel<Profile> implements ComboBoxModel<Profile> {

        protected final List<Profile> profiles = new ArrayList<>();
        protected Profile selectedItem;

        public ProfileListModel() {
            update();
        }

        public void update() {
            profiles.clear();
            final Path pd = Ide.getProfilesDir();
            try (final Stream<Path> stream = Files.walk(pd, 1)) {
                stream.filter(Files::isDirectory).filter(p -> !pd.equals(p)).map(Profile::new).forEach(profiles::add);
            } catch (Exception x) {
                warning("Unable to walk {0}", x, pd);
            }
            Collections.sort(profiles);
            fireContentsChanged(this, 0, getSize());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = (Profile) anItem;
        }

        @Override
        public Profile getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return profiles.size();
        }

        @Override
        public Profile getElementAt(int index) {
            return profiles.get(index);
        }
    }
}
