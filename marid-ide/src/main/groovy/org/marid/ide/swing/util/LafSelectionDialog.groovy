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

package org.marid.ide.swing.util

import groovy.transform.InheritConstructors
import org.marid.ide.Ide
import org.marid.swing.AbstractDialog

import javax.swing.*
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import javax.swing.UIManager.LookAndFeelInfo

import static javax.swing.UIManager.getInstalledLookAndFeels

/**
 * LAF selection dialog.
 *
 * @author Dmitry Ovchinnikov 
 */
@InheritConstructors
class LafSelectionDialog extends AbstractDialog {

    private final JList<Laf> lafs = new JList<>(installedLafs);

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        def sp = new JScrollPane(lafs);
        vg.addComponent(sp);
        hg.addComponent(sp);
        addDefaultButtons(gl, vg, hg);
    }

    @Override
    protected void accept() {
        def laf = lafs.selectedValue;
        if (laf != null) {
            Ide.application.preferences.put("laf", laf.className);
        }
    }

    private Laf[] getInstalledLafs() {
        return installedLookAndFeels.collect {new Laf(laf: it)};
    }

    private class Laf {
        @Delegate
        LookAndFeelInfo laf;

        @Override
        String toString() {
            return laf.name;
        }
    }
}
