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
import org.marid.nio.FileUtils.CopyTask;
import org.marid.nio.FileUtils.RemoveTask;
import org.marid.swing.AbstractDialog;
import org.marid.swing.MaridAction;
import org.marid.swing.MaridButtons;
import org.marid.swing.OutputArea;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class OutputBuilderDialogImpl extends AbstractDialog implements Dialog {

    private static final Logger LOG = Logger.getLogger(OutputBuilderDialogImpl.class.getName());
    private final JTabbedPane tabbedPane;

    public OutputBuilderDialogImpl(FrameImpl frame) {
        super(frame, "Output builder", false);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(S.l("Extract"), new ExtractTab());
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

    private class ExtractTab extends JPanel {

        private final Preferences prefs = preferences(ExtractTab.class, "output", "builder");
        private final JLabel zipLabel = new JLabel(S.l("ZIP URL") + ":");
        private final JTextField zipField = new JTextField(getDefaultZipPath(), 80);
        private final JButton zipBrowse = MaridButtons.browseButton(zipField);
        private final OutputArea outputArea = new OutputArea(20);
        private final JButton cleanButton = new JButton(getCleanAction());
        private final JButton extractButton = new JButton(getExtractAction());
        private final Path outDir = Paths.get(getOwner().getApplication().getOutputDirectory());

        public ExtractTab() {
            final GroupLayout g = new GroupLayout(this);
            g.setAutoCreateGaps(true);
            g.setAutoCreateContainerGaps(true);
            final GroupLayout.SequentialGroup v = g.createSequentialGroup();
            final GroupLayout.ParallelGroup h = g.createParallelGroup();
            final GroupLayout.SequentialGroup h1 = g.createSequentialGroup();
            v.addGroup(g.createParallelGroup(Alignment.BASELINE)
                    .addComponent(zipLabel)
                    .addComponent(zipField)
                    .addComponent(zipBrowse));
            v.addComponent(outputArea.getScrollPane());
            v.addGroup(g.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cleanButton)
                    .addComponent(extractButton));
            h1.addGroup(g.createParallelGroup()
                    .addComponent(zipLabel));
            h1.addGroup(g.createParallelGroup()
                    .addGroup(g.createSequentialGroup()
                            .addComponent(zipField)
                            .addComponent(zipBrowse)));
            h.addGroup(h1);
            h.addComponent(outputArea.getScrollPane());
            h.addGroup(g.createSequentialGroup()
                    .addComponent(cleanButton).addGap(0, 0, Integer.MAX_VALUE)
                    .addComponent(extractButton));
            g.setVerticalGroup(v);
            g.setHorizontalGroup(h);
            setLayout(g);
            outputArea.setEditable(false);
        }

        private String getDefaultZipPath() {
            String text = prefs.get("zip", null);
            if (text == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = getClass().getClassLoader();
                }
                final String version = Versioning.getImplementationVersion(Marid.class);
                final URL url = cl.getResource("marid-runtime-" + version + ".zip");
                if (url != null) {
                    try {
                        text = new File(url.toURI()).toString();
                    } catch (URISyntaxException x) {
                        warning(LOG, "Invalid ZIP path: {0}", x, url);
                    }
                }
            }
            return text != null ? text : "";
        }

        private void enableButtons(boolean state) {
            cleanButton.getAction().setEnabled(state);
            extractButton.getAction().setEnabled(state);
        }

        private MaridAction getCleanAction() {
            return new MaridAction("Clean") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enableButtons(false);
                    final AtomicLong counter = new AtomicLong();
                    final Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final RemoveTask removeTask = new RemoveTask(outDir) {
                                @Override
                                protected void remove(final Path path) throws IOException {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            outputArea.append("\t" + path + "\n");
                                        }
                                    });
                                    counter.incrementAndGet();
                                    super.remove(path);
                                }
                            };
                            try {
                                removeTask.call();
                            } catch (Exception x) {
                                warning(LOG, "Cleaning output execution exception", x);
                                outputArea.append(M.l("Error: {0}", x) + "\n");
                            } finally {
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableButtons(true);
                                        outputArea.append(M.l("Cleaned {0} files", counter.get()) + "\n");
                                    }
                                });
                            }
                        }
                    });
                    outputArea.setText("");
                    outputArea.append(M.l("Cleaning") + "...\n");
                    worker.start();
                }
            };
        }

        private MaridAction getExtractAction() {
            return new MaridAction("Extract") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enableButtons(false);
                    final AtomicLong counter = new AtomicLong();
                    final List<CopyTask> copyTasks = new ArrayList<>();
                    try {
                        final Path zipPath = Paths.get(zipField.getText());
                        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        final FileSystem zipfs = FileSystems.newFileSystem(zipPath, cl);
                        for (final Path root : zipfs.getRootDirectories()) {
                            copyTasks.add(new CopyTask(root, outDir) {
                                @Override
                                protected void copy(Path src, final Path dst) throws IOException {
                                    EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dst.equals(outDir)) {
                                                return;
                                            }
                                            outputArea.append("\t" + dst + "\n");
                                        }
                                    });
                                    counter.incrementAndGet();
                                    super.copy(src, dst);
                                }
                            });
                        }
                    } catch (Exception x) {
                        warning(LOG, "ZIP error", x);
                        return;
                    }
                    final Thread worker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                for (final CopyTask copyTask : copyTasks) {
                                    copyTask.call();
                                }
                            } catch (Exception x) {
                                warning(LOG, "Extracting to the output execution exception", x);
                                outputArea.append(M.l("Error: {0}", x) + "\n");
                            } finally {
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableButtons(true);
                                        outputArea.append(M.l("Extracted {0} files", counter.get()) + "\n");
                                    }
                                });
                            }
                        }
                    });
                    outputArea.setText("");
                    outputArea.append(M.l("Extracting") + "...\n");
                    worker.start();
                }
            };
        }
    }
}
