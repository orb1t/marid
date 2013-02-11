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

package org.marid.ide.gui.util

import groovy.transform.InheritConstructors
import groovy.util.logging.Log
import org.marid.ide.gui.AbstractDialog
import org.marid.image.MaridImage
import org.marid.l10n.Localized.S

import javax.swing.*
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

@Log
@InheritConstructors
class ImageGenDialog extends AbstractDialog {

    private JSpinner sizeSpinner;
    private JLabel iconArea;

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        def sizeLabel = new JLabel(S.l("Icon size (in pixels)") + ":");
        sizeSpinner = new JSpinner(new SpinnerNumberModel(prefs.getInt("size", 32), 16, 512, 2));
        sizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(ChangeEvent e) {
                iconArea.icon = new ImageIcon(MaridImage.getIcon(sizeSpinner.value));
                ImageGenDialog.this.validate();
                ImageGenDialog.this.pack();
                ImageGenDialog.this.locationRelativeTo = ImageGenDialog.this.owner;
            }
        })
        iconArea = new JLabel(new ImageIcon(MaridImage.getIcon(sizeSpinner.value)));
        vg.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(sizeLabel).addComponent(sizeSpinner));
        vg.addComponent(iconArea);
        hg.addGroup(gl.createSequentialGroup().addComponent(sizeLabel).addComponent(sizeSpinner));
        hg.addGroup(gl.createSequentialGroup()
                .addGap(0, 0, Integer.MAX_VALUE)
                .addComponent(iconArea)
                .addGap(0, 0, Integer.MAX_VALUE));
        addDefaultButtons(gl, vg, hg);
    }

    @Override
    protected void accept() {
        prefs.putInt("size", sizeSpinner.value);
    }
}
