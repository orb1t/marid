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

import org.marid.image.MaridImage;
import org.marid.swing.AbstractDialog;
import org.marid.swing.MaridAction;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.marid.groovy.MaridGroovyMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ImageGenDialog extends AbstractDialog {

    private final SpinnerNumberModel sizeModel = new SpinnerNumberModel(
            prefs.getInt("size", 32), 16, 512, 2);
    private final JSpinner sizeSpinner = new JSpinner(sizeModel);
    private final JLabel iconArea = new JLabel(MaridImage.getIcon((int) sizeSpinner.getValue()));
    private final List<? extends FileFilter> filters = Arrays.asList(
            new FileNameExtensionFilter(S.l("PNG files"), "png"),
            new FileNameExtensionFilter(S.l("JPG files"), "jpg")
    );
    private final String[] formats = {"png", "jpeg"};
    private final Action saveAction = new MaridAction("Save...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser(prefs.get("dir", "."));
            fc.setAcceptAllFileFilterUsed(true);
            fc.setMultiSelectionEnabled(true);
            for (FileFilter f : filters) {
                fc.addChoosableFileFilter(f);
            }
            fc.setFileFilter(filters.get(prefs.getInt("filter", 0)));
            int result = fc.showSaveDialog(ImageGenDialog.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                RenderedImage image = (RenderedImage) ((ImageIcon) iconArea.getIcon()).getImage();
                int filterIdx = filters.indexOf(fc.getFileFilter());
                String format = filterIdx >= 0 ? formats[filterIdx] : "png";
                File f = fc.getSelectedFile();
                if (!f.getName().endsWith(format.toLowerCase())) {
                    f = new File(f.getParentFile(), f.getName() + "." + format.toLowerCase());
                }
                try {
                    ImageIO.write(image, format, f);
                } catch (Exception x) {
                    warning(log, "Unable to save the file {0}", x, f);
                } finally {
                    prefs.put("dir", f.getParentFile().getAbsolutePath());
                    if (filterIdx >= 0) prefs.putInt("filter", filterIdx);
                }
            }
        }
    };

    {
        sizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                iconArea.setIcon(MaridImage.getIcon((int) sizeSpinner.getValue()));
                validate();
                pack();
                setLocationRelativeTo(getOwner());
            }
        });
    }

    public ImageGenDialog(Frame frame, boolean modal) {
        super(frame, "Marid image generation dialog", modal);
    }

    @Override
    protected void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        JLabel sizeLabel = new JLabel(S.l("Icon size (in pixels)") + ":");
        JButton saveButton = new JButton(saveAction);
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
        prefs.putInt("size", (int) sizeSpinner.getValue());
    }
}
