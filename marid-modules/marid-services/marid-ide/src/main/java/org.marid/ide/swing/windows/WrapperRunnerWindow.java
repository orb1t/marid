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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "network")
@Tab(node = "data")
@Tab(node = "source")
@Tab(node = "performance")
public class WrapperRunnerWindow extends AbstractMultiFrame {

    public WrapperRunnerWindow() {
        super("Wrapper Runner");
        pack();
    }

    @Input(tab = "source")
    public ControlContainer<URL, UrlInputControl> zipFile() {
        return ccs(new UrlInputControl("ZIP files", "zip"), () -> Utils.getResource("marid-wrapper-%s.zip", version));
    }

    @Input(tab = "network")
    public ControlContainer<Integer, FormattedIntInputControl> networkTimeoutInSeconds() {
        return ccv(new FormattedIntInputControl(), 3_600);
    }

    @Input(tab = "network")
    public ControlContainer<Integer, FormattedIntInputControl> port() {
        return ccv(new FormattedIntInputControl(), WrapperConstants.DEFAULT_PORT);
    }

    @Input(tab = "network")
    public ControlContainer<Integer, FormattedIntInputControl> backlog() {
        return ccv(new FormattedIntInputControl(), 5);
    }

    @Input(tab = "network")
    public ControlContainer<String, StringInputControl> host() {
        return ccv(new StringInputControl(), "");
    }

    @Input(tab = "performance")
    public ControlContainer<Integer, SpinIntInputControl> queueSize() {
        return ccv(new SpinIntInputControl(2, 128, 2), 8);
    }

    @Input(tab = "performance")
    public ControlContainer<Integer, SpinIntInputControl> threads() {
        return ccv(new SpinIntInputControl(1, 32, 1), 16);
    }

    @Input(tab = "data")
    public ControlContainer<File, FileInputControl> targetDirectory() {
        return ccv(new FileInputControl(), new File(System.getProperty("user.home"), "marid/wrapper"));
    }

    @FrameWidget(position = "c")
    @FrameAction(key = "F5", info = "Starts the wrapper", group = "control", tool = true, path = "Wrapper", icon = "start")
    public class Runner extends InternalFrame {

        public Runner(ActionEvent actionEvent, Action action) {
            setPreferredSize(new Dimension(800, 600));
            pack();
            action.setEnabled(false);
        }
    }
}
