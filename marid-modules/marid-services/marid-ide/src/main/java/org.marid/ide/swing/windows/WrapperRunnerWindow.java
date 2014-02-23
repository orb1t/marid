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

import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.FrameAction;
import org.marid.swing.FrameWidget;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.*;
import org.marid.util.Utils;
import org.marid.wrapper.WrapperConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "network")
@Tab(node = "data")
@Tab(node = "source")
@Tab(node = "performance")
@Tab(node = "jvm", label = "JVM")
public class WrapperRunnerWindow extends AbstractMultiFrame {

    @Input(tab = "source", order = 0)
    public final Pv<URL, UrlInputControl> zipFile = new Pv<>(
            () -> new UrlInputControl("ZIP files", "zip"),
            () -> Utils.getResource("marid-wrapper-%s.zip", version));

    @Input(tab = "network", order = 0)
    public final Pv<String, StringInputControl> host = new Pv<>(StringInputControl::new, () -> "localhost");

    @Input(tab = "network", order = 1)
    public final Pv<Integer, FormattedIntInputControl> port = new Pv<>(
            FormattedIntInputControl::new,
            () -> WrapperConstants.DEFAULT_WRAPPER_PORT);

    @Input(tab = "network", order = 2)
    public final Pv<Integer, FormattedIntInputControl> backlog = new Pv<>(FormattedIntInputControl::new, () -> 5);

    @Input(tab = "network", order = 3, label = "Socket Timeout In Seconds")
    public final Pv<Integer, FormattedIntInputControl> soTimeout = new Pv<>(FormattedIntInputControl::new, () -> 3_600);

    @Input(tab = "performance", order = 0)
    public final Pv<Integer, SpinIntInputControl> queueSize = new Pv<>(() -> new SpinIntInputControl(2, 128, 2), () -> 8);

    @Input(tab = "performance", order = 1)
    public final Pv<Integer, SpinIntInputControl> threads = new Pv<>(() -> new SpinIntInputControl(1, 32, 1), () -> 16);

    @Input(tab = "data", order = 0)
    public final Pv<File, FileInputControl> targetDirectory = new Pv<>(
            FileInputControl::new,
            () -> new File(System.getProperty("user.home"), "marid/wrapper"));

    @Input(tab = "data", order = 1)
    public final Pv<File, FileInputControl> logsDirectory = new Pv<>(
            FileInputControl::new,
            () -> new File(targetDirectory.get(), "logs"));

    @Input(tab = "jvm", order = 0, label = "JVM Path")
    public final Pv<String, StringInputControl> jvmPath = new Pv<>(
            StringInputControl::new,
            () -> Paths.get(System.getProperty("java.home"), "bin", "java").toString()
    );

    @Input(tab = "jvm", order = 1, label = "JVM arguments")
    public final Pv<String, StringInputControl> jvmArgs = new Pv<>(StringInputControl::new, () -> "");

    public WrapperRunnerWindow() {
        super("Wrapper Runner");
        pack();
    }

    @FrameWidget
    @FrameAction(key = "F5", info = "Starts the wrapper", group = "control", tool = true, path = "Wrapper", icon = "start")
    public class Runner extends InternalFrame {

        public Runner(ActionEvent actionEvent, Action action) {
            pack();
            action.setEnabled(false);
        }
    }
}
