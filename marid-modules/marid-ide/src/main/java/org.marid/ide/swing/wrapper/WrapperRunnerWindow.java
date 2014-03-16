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

package org.marid.ide.swing.wrapper;

import org.marid.nio.FileUtils;
import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.control.ConsoleArea;
import org.marid.swing.menu.MenuActionList;
import org.marid.swing.process.ProcessWorker;
import org.marid.wrapper.Wrapper;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.io.File;
import java.nio.file.Files;
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
import static org.marid.net.UdpShutdownThread.sendShutdownSequence;

/**
 * @author Dmitry Ovchinnikov
 */
public class WrapperRunnerWindow extends AbstractMultiFrame implements WrapperRunnerConfiguration {

    private Action stopAction;

    public WrapperRunnerWindow() {
        super("Wrapper Runner");
        pack();
    }

    @Override
    protected void fillActions(MenuActionList actionList) {
        action("mainMenu", "Wrapper").put(actionList);
        stopAction = action("control", "Stop", "stop", true, "Wrapper")
                .setListener((a, ev) -> sendShutdownSequence(bindAddress.get(), "marid-wrapper"))
                .setInitializer(a -> a.setEnabled(false))
                .put(actionList);
        action("control", "Start", "start", true, "Wrapper")
                .setListener((a, ev) -> {
                    showFrame(Output.class);
                    a.setEnabled(false);
                    stopAction.setEnabled(true);
                })
                .put(actionList);
    }

    public class Output extends InternalFrame {

        private final ConsoleArea outArea = new ConsoleArea();
        private final ConsoleArea errArea = new ConsoleArea();
        private ProcessWorker worker;

        public Output() {
            super("wrapper", "Wrapper", false);
            final JTabbedPane pane = new JTabbedPane();
            add(pane);
            pane.addTab(s("Output"), outArea.wrap());
            pane.addTab(s("Errors"), errArea.wrap());
            pane.setSelectedIndex(1);
            pack();
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
                final ArrayList<String> args = new ArrayList<>();
                args.add(jvmPath.get());
                args.addAll(Arrays.asList(jvmArgs.get()));
                args.add("-cp");
                args.add(System.getProperty("java.class.path"));
                args.add(Wrapper.class.getName());
                args.add("-b");
                args.add(bindAddress.get().getHostString() + ":" + bindAddress.get().getPort());
                args.add("start");
                info("ProcessBuilder will run with: {0}", String.join(" ", args));
                return new ProcessBuilder(args).directory(dir);
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
                    stopAction.setEnabled(false);
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
