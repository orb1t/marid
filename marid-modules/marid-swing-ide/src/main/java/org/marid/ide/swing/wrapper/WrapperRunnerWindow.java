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

import org.marid.io.ProcessUtils;
import org.marid.nio.FileUtils;
import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.control.ConsoleArea;
import org.marid.swing.menu.MenuActionList;
import org.marid.swing.process.ProcessWorker;
import org.marid.wrapper.Wrapper;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.swing.JOptionPane.*;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.net.UdpShutdownThread.sendShutdownSequence;
import static org.marid.swing.util.MessageType.INFO;
import static org.marid.swing.util.MessageType.WARNING;

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
        actionList.add("mainMenu", "Wrapper");
        stopAction = actionList.add(true, "control", "Stop", "Wrapper")
                .setIcon("stop")
                .setListener((a, ev) -> sendShutdownSequence(bindAddress.get(), "marid-wrapper"))
                .setInitializer(a -> a.setEnabled(false));
        actionList.add(true, "control", "Start", "Wrapper")
                .setIcon("start")
                .setListener((a, ev) -> {
                    showSingletonFrame(Output::new);
                    a.setEnabled(false);
                    stopAction.setEnabled(true);
                });
    }

    public class Output extends SingletonIntFrame {

        private final ConsoleArea outArea = new ConsoleArea();
        private final ConsoleArea errArea = new ConsoleArea();
        private ProcessWorker worker;

        public Output() {
            super("Wrapper");
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

        private ProcessBuilder createProcessBuilder() {
            try {
                final File dir = targetDirectory.get();
                if (!checkTargetDirectory(dir)) {
                    throw new IllegalStateException("Target directory will not be created");
                }
                if (!dir.mkdirs()) {
                    warning("Unable to create directory: {0}", dir);
                    throw new IllegalStateException("Unable to create directory");
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
            } catch (IllegalStateException x) {
                throw x;
            } catch (Exception x) {
                warning("Unable to get process builder", x);
                throw new IllegalStateException(x);
            }
        }

        @Override
        public void show() {
            super.show();
            final ProcessBuilder processBuilder = createProcessBuilder();
            worker = new ProcessWorker(processBuilder, 10_000L) {
                @Override
                protected Process newProcess() throws IOException {
                    final Process process = super.newProcess();
                    final int pid = ProcessUtils.getPid(process);
                    if (pid >= 0) {
                        EventQueue.invokeLater(() -> setTitle(getTitle() + ": " + ProcessUtils.getPid(process)));
                    }
                    return process;
                }

                @Override
                protected void process(List<ProcessLine> chunks) {
                    for (final ProcessLine processLine : chunks) {
                        (processLine.error ? errArea : outArea).println(processLine.line);
                    }
                }
            };
            worker.execute();
            new Timer(100, e -> {
                if (worker.isDone()) {
                    ((Timer) e.getSource()).stop();
                    stopAction.setEnabled(false);
                    try {
                        showMessage(INFO, "Process result", "Process was terminated with exit code {0}", worker.get());
                    } catch (Exception x) {
                        warning("Process consume error", x);
                        showMessage(WARNING, "Process result", "Process was terminated with error", x);
                    }
                }
            }).start();
        }
    }
}
