/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.swing.impl.dialogs;

import org.marid.ide.itf.Dialog;
import org.marid.ide.swing.impl.FrameImpl;
import org.marid.swing.AbstractDialog;
import org.marid.swing.MaridAction;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("serial")
public class PreferencesDialogImpl extends AbstractDialog implements Dialog {

    private static final long serialVersionUID = -5800791474984622058L;
    private final JTabbedPane tabbedPane;
    private final Preferences pref = preferences("preferences");

    public PreferencesDialogImpl(FrameImpl frame) {
        super(frame, "Preferences", true);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(S.l("Common"), new CommonTab());
    }

    @Override
    public FrameImpl getOwner() {
        return (FrameImpl) super.getOwner();
    }

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        vg.addComponent(tabbedPane);
        hg.addComponent(tabbedPane);
        addDefaultButtons(gl, vg, hg);
    }

    private class CommonTab extends JPanel {

        private final JLabel libDirectoryLabel = new JLabel(S.l("Lib directory") + ":");
        private final JTextField libDirectoryText = new JTextField(
                getOwner().getApplication().getLibDirectory(), 40);
        private final Action libDirBrowse = new MaridAction("Browse", "browse.png") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
        private final JButton libDirBrowseButton = new JButton(libDirBrowse);

        public CommonTab() {
            GroupLayout g = new GroupLayout(this);
            g.setAutoCreateContainerGaps(true);
            g.setAutoCreateGaps(true);
            GroupLayout.SequentialGroup v = g.createSequentialGroup();
            GroupLayout.SequentialGroup h = g.createSequentialGroup();
            v.addGroup(g.createParallelGroup(Alignment.BASELINE)
                    .addComponent(libDirectoryLabel)
                    .addComponent(libDirectoryText)
                    .addComponent(libDirBrowseButton));
            h.addGroup(g.createParallelGroup()
                    .addComponent(libDirectoryLabel));
            h.addGroup(g.createParallelGroup()
                    .addGroup(g.createSequentialGroup()
                            .addComponent(libDirectoryText)
                            .addComponent(libDirBrowseButton)));
            g.setVerticalGroup(v);
            g.setHorizontalGroup(h);
            setLayout(g);
        }
    }
}
