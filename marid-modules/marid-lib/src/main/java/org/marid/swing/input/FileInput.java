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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileInput extends JPanel implements Input<String> {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());

    private final JTextField textField;

    public FileInput(String path, final FileFilter... fileFilters) {
        final GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        textField = new JTextField(get(path));
        final JButton browseButton = new JButton(new AbstractAction("...") {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                    switch (ch.showOpenDialog(FileInput.this)) {
                        case JFileChooser.APPROVE_OPTION:
                            textField.setText(ch.getSelectedFile().getAbsolutePath());
                            break;
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to show file chooser", x);
                }
            }
        });
        g.setVerticalGroup(g.createParallelGroup().addComponent(textField).addComponent(browseButton));
        g.setHorizontalGroup(g.createSequentialGroup().addComponent(textField).addComponent(browseButton));
        setLayout(g);
    }

    public FileInput(URL url, FileFilter... fileFilters) {
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
    public void putValue(Preferences preferences, String key) {
        preferences.put(key, textField.getText());
    }

    @Override
    public void loadValue(Preferences preferences, String key, String def) {
        textField.setText(preferences.get(key, def));
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
