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

package org.marid.wrapper;

import org.marid.concurrent.MaridTimerTask;
import org.marid.logging.Logging;
import org.marid.management.JmxUtils;
import org.marid.net.UdpShutdownThread;
import org.marid.util.Utils;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import java.util.Timer;
import java.util.logging.Logger;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static javax.management.remote.JMXConnectorServerFactory.newJMXConnectorServer;
import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper {

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());

    public static void main(String... args) throws Exception {
        Logging.init(Wrapper.class, "marid-wrapper-logging.properties");
        final WrapperCli cli = new WrapperCli(Utils.loadProperties(Wrapper.class, "marid-wrapper.properties"), args);
        if (cli.isHelp()) {
            cli.showHelp();
            return;
        }
        new Timer(true).schedule(new MaridTimerTask(() -> info(LOG, "{0}", Math.random())), 1000L, 1000L);
        switch (cli.getCommand()) {
            case "start":
                start(cli);
                break;
            case "stop":
                stop(cli);
                break;
            default:
                throw new IllegalArgumentException(cli.getCommand());
        }
    }

    private static void start(WrapperCli cli) throws Exception {
        final String name = cli.getInstanceName();
        final WrapperService service = new WrapperService(cli);
        getPlatformMBeanServer().registerMBean(service, WrapperConstants.WRAPPER_OBJECT_NAME);
        final JMXServiceURL url = cli.getJmxUrl();
        final JMXConnectorServer srv = newJMXConnectorServer(url, cli.getJmxEnv(), getPlatformMBeanServer());
        cli.applyForwarder(srv);
        JmxUtils.startServer(srv, url);
        info(LOG, "Started JMX connector server at {0}", url);
        final UdpShutdownThread thread = new UdpShutdownThread(name, service::start, cli.getBindAddress());
        try {
            thread.start();
            thread.join();
            srv.stop();
        } catch (Exception x) {
            warning(LOG, "Starting {0} error", x, name);
        }
        System.exit(thread.getExitCode());
    }

    private static void stop(WrapperCli cli) throws Exception {
        UdpShutdownThread.sendShutdownSequence(cli.getBindAddress(), cli.getInstanceName());
    }
}
