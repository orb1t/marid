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

import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.*;
import org.marid.util.Utils;
import org.marid.wrapper.Wrapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Paths;

import static java.lang.System.getProperty;
import static org.marid.Versioning.getImplementationVersion;
import static org.marid.wrapper.WrapperConstants.DEFAULT_JMX_ADDRESS;
import static org.marid.wrapper.WrapperConstants.DEFAULT_WRAPPER_SHUTDOWN_PORT;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "network")
@Tab(node = "data")
@Tab(node = "source")
@Tab(node = "performance")
@Tab(node = "jvm", label = "JVM")
public interface WrapperRunnerConfiguration extends Configuration {

    @Input(tab = "source", order = 0)
    Pv<URL, UrlInputControl> zipFile = new Pv<>(
            () -> new UrlInputControl("ZIP files", "zip"),
            () -> Utils.getResource("marid-wrapper-%s.zip", getImplementationVersion(Wrapper.class)));

    @Input(tab = "network", order = 1)
    Pv<String, StringInputControl> jmxAddress = new Pv<>(StringInputControl::new, () -> DEFAULT_JMX_ADDRESS);

    @Input(tab = "network", order = 2)
    Pv<InetSocketAddress, InetSocketAddressInputControl> bindAddress = new Pv<>(
            InetSocketAddressInputControl::new,
            () -> new InetSocketAddress("localhost", DEFAULT_WRAPPER_SHUTDOWN_PORT));

    @Input(tab = "performance", order = 0)
    Pv<Integer, SpinIntInputControl> queueSize = new Pv<>(() -> new SpinIntInputControl(2, 128, 2), () -> 8);

    @Input(tab = "performance", order = 1)
    Pv<Integer, SpinIntInputControl> threads = new Pv<>(() -> new SpinIntInputControl(1, 32, 1), () -> 16);

    @Input(tab = "data", order = 0)
    Pv<File, FileInputControl> targetDirectory = new Pv<>(
            FileInputControl::new, () -> new File(getProperty("user.home"), "marid"));

    @Input(tab = "jvm", order = 0, label = "JVM Path")
    Pv<String, StringInputControl> jvmPath = new Pv<>(
            StringInputControl::new, () -> Paths.get(getProperty("java.home"), "bin", "java").toString());

    @Input(tab = "jvm", order = 1, label = "JVM arguments")
    Pv<String[], StringArrayInputControl> jvmArgs = new Pv<>(StringArrayInputControl::new, () -> new String[0]);
}
