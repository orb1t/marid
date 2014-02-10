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

package org.marid.swing.input;

import org.marid.swing.MaridAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileInputControl extends JPanel implements InputControl<String, FileInputControl> {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());

    private final JTextField textField;

    public FileInputControl(String filePath, final FileFilter... fileFilters) {
        final GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        textField = new JTextField(get(filePath));
        final JButton browseButton = new JButton(new MaridAction("...", null, (a, e) -> {
            try {
                final String path = textField.getText().trim();
                final File parent = path.isEmpty() ? new File(".") : new File(path).getParentFile();
                final JFileChooser ch = new JFileChooser(parent == null ? new File(".") : parent);
                ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
                ch.setAcceptAllFileFilterUsed(true);
                for (final FileFilter fileFilter : fileFilters) {
                    ch.addChoosableFileFilter(fileFilter);
                }
                if (fileFilters.length > 0) {
                    ch.setFileFilter(fileFilters[0]);
                }
                ch.setMultiSelectionEnabled(false);
                if (!path.isEmpty() && new File(path).exists()) {
                    ch.setSelectedFile(new File(path));
                }
                switch (ch.showOpenDialog(FileInputControl.this)) {
                    case JFileChooser.APPROVE_OPTION:
                        textField.setText(ch.getSelectedFile().getAbsolutePath());
                        break;
                }
            } catch (Exception x) {
                warning(LOG, "Unable to show file chooser", x);
            }
        }));
        g.setVerticalGroup(g.createParallelGroup().addComponent(textField).addComponent(browseButton));
        g.setHorizontalGroup(g.createSequentialGroup().addComponent(textField, 0, 300, -1).addComponent(browseButton));
        setLayout(g);
    }

    public FileInputControl(URL url, FileFilter... fileFilters) {
        this(url == null ? "" : new File(get(url)).getAbsolutePath(), fileFilters);
    }

    @Override
    public String getValue() {
        return textField.getText();
    }

    @Override
    public void setValue(String value) {
        textField.setText(value);
    }

    @Override
    public int getBaseline(int width, int height) {
        return textField.getBaseline(width, height);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return textField.getBaselineResizeBehavior();
    }

    private static String get(String path) {
        try {
            return path == null ? "" : new File(path).getAbsolutePath();
        } catch (Exception x) {
            warning(LOG, "Illegal path: {0}", x, path);
            return "";
        }
    }

    private static String get(URL url) {
        try {
            return url == null ? "" : new File(url.toURI()).getAbsolutePath();
        } catch (Exception x) {
            warning(LOG, "Illegal URL: {0}", x, url);
            return "";
        }
    }
}
