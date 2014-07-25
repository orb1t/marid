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

import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.actions.WindowAction;
import org.marid.swing.pref.SwingPrefCodecs;
import org.marid.util.SysPropsSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
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
    protected final JComboBox<Path> profilesCombo = new JComboBox<>(profileListModel);
    protected volatile Path profilesDir = Ide.getProfilesDir();

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
        SwingPrefCodecs.addConsumer(ideFrame, Path.class, SYSPREFS, "profilesDir", p -> {
            profilesDir = Ide.getProfilesDir();
        });
        final Timer timer = new Timer(1_000, e -> timeLabel.setText(currentTime()));
        ideFrame.addWindowListener(new WindowAction(e -> {
            switch (e.getID()) {
                case WindowEvent.WINDOW_ICONIFIED:
                case WindowEvent.WINDOW_CLOSED:
                    timer.stop();
                    break;
                case WindowEvent.WINDOW_OPENED:
                    profileListModel.update();
                case WindowEvent.WINDOW_DEICONIFIED:
                    timer.start();
                    break;
            }
        }));
        profilesCombo.setRenderer(new DirectoryRenderer());
    }

    private String currentTime() {
        return dateFormat.format(new Date());
    }

    protected class DirectoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
            final DirectoryRenderer renderer = (DirectoryRenderer) super.getListCellRendererComponent(l, v, i, s, f);
            if (v instanceof Path) {
                renderer.setText(((Path) v).getFileName().toString());
            }
            return renderer;
        }
    }

    protected class ProfileListModel extends AbstractListModel<Path> implements ComboBoxModel<Path> {

        protected final List<Path> directories = new ArrayList<>();
        protected Path selectedItem;

        public void update() {
            directories.clear();
            try (final Stream<Path> stream = Files.walk(profilesDir)) {
                stream.filter(Files::isDirectory).forEach(directories::add);
            } catch (Exception x) {
                warning("Unable to walk {0}", x, profilesDir);
            }
            fireContentsChanged(this, 0, getSize());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = (Path) anItem;
        }

        @Override
        public Path getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return directories.size();
        }

        @Override
        public Path getElementAt(int index) {
            return directories.get(index);
        }
    }
}
