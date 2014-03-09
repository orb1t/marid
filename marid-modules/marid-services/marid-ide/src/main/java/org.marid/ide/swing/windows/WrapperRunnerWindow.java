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

package org.marid.ide.swing.windows;

import org.marid.nio.FileUtils;
import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.FrameAction;
import org.marid.swing.FrameWidget;
import org.marid.swing.control.ConsoleArea;
import org.marid.swing.process.ProcessWorker;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static javax.swing.JOptionPane.*;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.nio.FileUtils.CopyTask;

/**
 * @author Dmitry Ovchinnikov
 */
public class WrapperRunnerWindow extends AbstractMultiFrame implements WrapperRunnerConfiguration {

    public WrapperRunnerWindow() {
        super("Wrapper Runner");
        pack();
    }

    @FrameWidget
    @FrameAction(key = "F5", info = "Starts the wrapper", group = "control", tool = true, path = "Wrapper", icon = "start")
    public class Output extends InternalFrame {

        private final ConsoleArea outArea = new ConsoleArea();
        private final ConsoleArea errArea = new ConsoleArea();
        private ProcessWorker worker;

        public Output(ActionEvent actionEvent, Action action) {
            final JTabbedPane pane = new JTabbedPane();
            add(pane);
            pane.addTab(s("Output"), outArea.wrap());
            pane.addTab(s("Errors"), errArea.wrap());
            pane.setSelectedIndex(1);
            pack();
            action.setEnabled(false);
            addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    worker.terminate();
                }
            });
        }

        private boolean checkTargetDirectory(File dir) throws Exception {
            if (!dir.exists()) {
                return true;
            }
            switch (showConfirmDialog(this, m("Target directory {0} exists. Delete it?", dir), s("Existing directory"), YES_NO_OPTION, QUESTION_MESSAGE)) {
                case JOptionPane.YES_OPTION:
                    Files.walkFileTree(dir.toPath(), FileUtils.RECURSIVE_CLEANER);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void show() {
            super.show();
            final Future<ProcessBuilder> zipResult = newSingleThreadExecutor().submit(() -> {
                final File dir = targetDirectory.get();
                if (!checkTargetDirectory(dir)) {
                    return null;
                }
                if (!dir.mkdirs()) {
                    warning("Unable to create directory: {0}", dir);
                    return null;
                }
                try (final FileSystem zipFs = FileSystems.newFileSystem(Paths.get(zipFile.get().toURI()), null)) {
                    for (final Path path : zipFs.getRootDirectories()) {
                        if (!new CopyTask(path, dir.toPath()) {
                            @Override
                            protected void copy(Path src, Path dst) throws IOException {
                                super.copy(src, dst);
                                info("Copied {0} to {1}", src, dst);
                            }
                        }.call()) {
                            return null;
                        }
                    }
                }
                final File wd = new File(dir, "marid-wrapper");
                final ArrayList<String> args = new ArrayList<>();
                args.add(jvmPath.get());
                args.addAll(Arrays.asList(jvmArgs.get()));
                args.add("-jar");
                args.addAll(Arrays.asList(wd.list((f, n) -> n.endsWith(".jar"))));
                args.add("-b");
                args.add(bindAddress.get().toString());
                args.add("start");
                info("ProcessBuilder will run with: {0}", String.join(" ", args));
                return new ProcessBuilder(args).directory(wd);
            });
            new Timer(100, e -> {
                if (worker == null) {
                    if (zipResult.isDone()) {
                        try {
                            final ProcessBuilder processBuilder = zipResult.get();
                            worker = new ProcessWorker(processBuilder, 10_000L) {
                                @Override
                                protected void process(List<ProcessLine> chunks) {
                                    for (final ProcessLine processLine : chunks) {
                                        (processLine.error ? errArea : outArea).println(processLine.line);
                                    }
                                    if (!errorQueue.isEmpty()) {
                                        for (final Iterator<Exception> i = errorQueue.iterator(); i.hasNext(); ) {
                                            warning("Process error", i.next());
                                            i.remove();
                                        }
                                    }
                                }
                            };
                            worker.execute();
                        } catch (Exception x) {
                            ((Timer) e.getSource()).stop();
                            final Throwable ex = x instanceof ExecutionException ? x.getCause() : x;
                            warning("Unable to extract ZIP archive", ex);
                            showMessageDialog(this, m("Unable to extract ZIP archive: {0}", ex), s("Extract result"), WARNING_MESSAGE);
                        }
                    }
                } else if (worker.isDone()) {
                    ((Timer) e.getSource()).stop();
                    try {
                        showMessageDialog(this, m("Process was terminated with exit code {0}", worker.get()), s("Process result"), INFORMATION_MESSAGE);
                    } catch (Exception x) {
                        final Throwable ex = x instanceof ExecutionException ? x.getCause() : x;
                        warning("Process consume error", ex);
                        showMessageDialog(this, m("Process was terminated with error {0}", ex), s("Process result"), WARNING_MESSAGE);
                    }
                }
            }).start();
        }
    }
}
