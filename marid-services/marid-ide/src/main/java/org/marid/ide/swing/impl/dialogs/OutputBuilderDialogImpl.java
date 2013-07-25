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

import org.marid.Marid;
import org.marid.Versioning;
import org.marid.ide.itf.Dialog;
import org.marid.ide.swing.impl.FrameImpl;
import org.marid.swing.AbstractDialog;
import org.marid.swing.MaridButtons;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.net.URL;
import java.util.prefs.Preferences;

import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class OutputBuilderDialogImpl extends AbstractDialog implements Dialog {

    private final JTabbedPane tabbedPane;

    public OutputBuilderDialogImpl(FrameImpl frame) {
        super(frame, "Output builder", false);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(S.l("Build"), new BuildTab());
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

    private class BuildTab extends JPanel {

        private final Preferences prefs = preferences(BuildTab.class, "output", "builder");
        private final JLabel zipUrlLabel = new JLabel(S.l("ZIP url") + ":");
        private final JTextField zipUrlField = new JTextField(getDefaultZipUrl(), 80);
        private final JButton zipBrowse = MaridButtons.browseUrlButton(zipUrlField);

        public BuildTab() {
            GroupLayout g = new GroupLayout(this);
            g.setAutoCreateGaps(true);
            g.setAutoCreateContainerGaps(true);
            GroupLayout.SequentialGroup v = g.createSequentialGroup();
            GroupLayout.SequentialGroup h = g.createSequentialGroup();
            v.addGroup(g.createParallelGroup(Alignment.BASELINE)
                    .addComponent(zipUrlLabel)
                    .addComponent(zipUrlField)
                    .addComponent(zipBrowse));
            h.addGroup(g.createParallelGroup()
                    .addComponent(zipUrlLabel));
            h.addGroup(g.createParallelGroup()
                    .addGroup(g.createSequentialGroup()
                            .addComponent(zipUrlField)
                            .addComponent(zipBrowse)));
            g.setVerticalGroup(v);
            g.setHorizontalGroup(h);
            setLayout(g);
        }

        private String getDefaultZipUrl() {
            String urlText = prefs.get("zipUrl", null);
            if (urlText == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = getClass().getClassLoader();
                }
                String version = Versioning.getImplementationVersion(Marid.class);
                URL url = cl.getResource("marid-runtime-" + version + ".zip");
                if (url != null) {
                    urlText = url.toString();
                }
            }
            return urlText != null ? urlText : "";
        }
    }
}
