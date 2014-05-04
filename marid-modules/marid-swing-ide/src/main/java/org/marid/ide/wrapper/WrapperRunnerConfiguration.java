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

package org.marid.ide.wrapper;

import org.marid.l10n.L10n;
import org.marid.swing.MaridAction;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.*;

import javax.swing.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Paths;

import static java.lang.System.getProperty;
import static org.marid.wrapper.WrapperConstants.DEFAULT_JMX_ADDRESS;
import static org.marid.wrapper.WrapperConstants.DEFAULT_WRAPPER_SHUTDOWN_PORT;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "network")
@Tab(node = "data")
@Tab(node = "performance")
@Tab(node = "jvm", label = "JVM")
public interface WrapperRunnerConfiguration extends Configuration {

    @Input(tab = "network", order = 1, name = "JMX address")
    Pv<String> JMX_ADDRESS = new Pv<>(StringInputControl::new, () -> DEFAULT_JMX_ADDRESS);

    @Input(tab = "network", order = 2)
    Pv<InetSocketAddress> BIND_ADDRESS = new Pv<>(
            InetSocketAddressInputControl::new,
            () -> new InetSocketAddress("localhost", DEFAULT_WRAPPER_SHUTDOWN_PORT));

    @Input(tab = "performance", order = 0)
    Pv<Integer> QUEUE_SIZE = new Pv<>(() -> new SpinIntInputControl(2, 128, 2), () -> 8);

    @Input(tab = "performance", order = 1)
    Pv<Integer> THREADS = new Pv<>(() -> new SpinIntInputControl(1, 32, 1), () -> 16);

    @Input(tab = "data", order = 0)
    Pv<File> TARGET_DIRECTORY = new Pv<>(
            FileInputControl::new, () -> new File(new File(getProperty("user.home"), "marid"), "marid-wrapper"));

    @Input(tab = "jvm", order = 0, label = "JVM Path")
    Pv<String> JVM_PATH = new Pv<>(
            StringInputControl::new, () -> Paths.get(getProperty("java.home"), "bin", "java").toString());

    @Input(tab = "jvm", order = 1, label = "JVM arguments")
    Pv<String[]> JVM_ARGS = new Pv<>(() -> new StringArrayInputControl().withToolbar((m, t) -> {
        t.add(new MaridAction("Add debug parameters", "bug", (a, e) -> {
            final String port = JOptionPane.showInputDialog(L10n.m("Input debug port") + ": ", "5005");
            if (port != null) {
                m.addElement(String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%s", port));
            }
        })).setFocusable(false);
    }), () -> new String[0]);
}
