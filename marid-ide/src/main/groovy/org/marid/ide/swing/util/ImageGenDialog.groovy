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
import groovy.util.logging.Log
import org.marid.ide.swing.AbstractDialog
import org.marid.ide.res.MaridAction
import org.marid.image.MaridImage

import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.event.ActionEvent
import java.awt.image.RenderedImage

@Log
@InheritConstructors
class ImageGenDialog extends AbstractDialog {

    private final def sizeModel = new SpinnerNumberModel(prefs.getInt("size", 32), 16, 512, 2);
    private final def sizeSpinner = new JSpinner(sizeModel);
    private final def iconArea = new JLabel(MaridImage.getIcon(sizeSpinner.value as int));
    private final def filters = [
            new FileNameExtensionFilter("PNG files".ls(), "png"),
            new FileNameExtensionFilter("JPG files".ls(), "jpg")
    ];
    private final def formats = [
            "png", "jpeg"
    ];
    private final def saveAction = new MaridAction(name: "Save...") {
        @Override
        void actionPerformed(ActionEvent e) {
            def fc = new JFileChooser(prefs.get("dir", "."));
            fc.acceptAllFileFilterUsed = true;
            fc.multiSelectionEnabled = false;
            filters.each { fc.addChoosableFileFilter(it) };
            fc.fileFilter = filters[prefs.getInt("filter", 0)];
            def result = fc.showSaveDialog(ImageGenDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                def image = ((ImageIcon) iconArea.icon).image;
                if (image instanceof RenderedImage) {
                    image = (RenderedImage) image;
                    def filterIdx = filters.indexOf(fc.fileFilter);
                    def format = filterIdx >= 0 ? formats[filterIdx] : "png";
                    def file = fc.selectedFile;
                    if (!file.name.endsWith(format.toLowerCase())) {
                        file = new File(file.parentFile, file.name + "." + format.toLowerCase());
                    }
                    try {
                        ImageIO.write(image, format, file);
                    } catch (x) {
                        log.warning("Unable to save the file {0}", x, file);
                    } finally {
                        prefs.put("dir", file.parentFile.absolutePath);
                        if (filterIdx >= 0) prefs.putInt("filter", filterIdx);
                    }
                }
            }
        }
    };

    {
        sizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(ChangeEvent e) {
                iconArea.icon = MaridImage.getIcon(sizeSpinner.value as int);
                ImageGenDialog.this.validate();
                ImageGenDialog.this.pack();
                ImageGenDialog.this.locationRelativeTo = ImageGenDialog.this.owner;
            }
        });
    }

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        def sizeLabel = new JLabel("Icon size (in pixels)".ls() + ":");
        def saveButton = new JButton(saveAction);
        vg.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(sizeLabel).addComponent(sizeSpinner));
        vg.addComponent(iconArea);
        vg.addComponent(saveButton);
        hg.addGroup(gl.createSequentialGroup().addComponent(sizeLabel).addComponent(sizeSpinner));
        hg.addGroup(gl.createSequentialGroup()
                .addGap(0, 0, Integer.MAX_VALUE)
                .addComponent(iconArea)
                .addGap(0, 0, Integer.MAX_VALUE));
        hg.addGroup(gl.createSequentialGroup()
                .addGap(0, 0, Integer.MAX_VALUE)
                .addComponent(saveButton)
                .addGap(0, 0, Integer.MAX_VALUE));
        addDefaultButtons(gl, vg, hg);
    }

    @Override
    protected void accept() {
        prefs.putInt("size", sizeSpinner.value as int);
    }
}
