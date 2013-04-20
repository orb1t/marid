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

package org.marid.swing.dialogs;

import org.marid.swing.AbstractDialog;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.UIManager.*;
import java.awt.*;

import static javax.swing.UIManager.*;
import static org.marid.groovy.MaridGroovyMethods.*;


/**
 * @author Dmitry Ovchinnikov
 */
public class LafSelectionDialog extends AbstractDialog {

    private final JList<Laf> lafs = new JList<>(getInstalledLafs());

    public LafSelectionDialog(Frame frame, boolean modal) {
        super(frame, "Look and feel selection dialog", modal);
    }

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        JScrollPane sp = new JScrollPane(lafs);
        vg.addComponent(sp);
        hg.addComponent(sp);
        addDefaultButtons(gl, vg, hg);
    }

    @Override
    protected void accept() {
        Laf laf = lafs.getSelectedValue();
        if (laf != null) {
            getPreferences(getClass()).put("laf", laf.toString());
        }
    }

    private Laf[] getInstalledLafs() {
        LookAndFeelInfo[] lafInfos = getInstalledLookAndFeels();
        Laf[] lafs = new Laf[lafInfos.length];
        for (int i = 0; i < lafInfos.length; i++) {
            lafs[i] = new Laf(lafInfos[i]);
        }
        return lafs;
    }

    private class Laf {

        final LookAndFeelInfo laf;

        Laf(LookAndFeelInfo laf) {
            this.laf = laf;
        }

        @Override
        public String toString() {
            return laf.getName();
        }
    }
}
