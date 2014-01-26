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

import org.marid.l10n.L10n;
import org.marid.swing.AbstractMultiFrame;
import org.marid.swing.FrameAction;
import org.marid.swing.FrameWidget;
import org.marid.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * @author Dmitry Ovchinnikov
 */
public class WrapperRunnerWindow extends AbstractMultiFrame {

    private final URL wrapperUrl = Utils.getClassLoader(getClass()).getResource("marid-wrapper-" + version + ".zip");

    public WrapperRunnerWindow() {
        super("Wrapper Runner");
        pack();
    }

    @FrameAction(key = "F5", info = "Starts the wrapper", group = "control", tool = true, path = "Wrapper")
    public void start(ActionEvent actionEvent, Action action) {
        showFrame("runner");
        action.setEnabled(false);
    }

    @FrameWidget(name = "runner", user = false)
    public class Runner extends JInternalFrame {

        public Runner() {
            super(L10n.s("Runner"), true, false, true, false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setPreferredSize(new Dimension(800, 600));
            pack();
        }
    }
}
