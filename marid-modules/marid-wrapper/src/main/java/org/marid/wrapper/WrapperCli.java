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

package org.marid.wrapper;

import com.sun.jmx.remote.security.MBeanServerFileAccessController;
import org.apache.commons.cli.*;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Properties;

import static org.marid.l10n.L10n.s;
import static org.marid.util.Utils.getUrl;
import static org.marid.util.Utils.loadProperties;
import static org.marid.wrapper.WrapperConstants.DEFAULT_JMX_ADDRESS;
import static org.marid.wrapper.WrapperConstants.DEFAULT_WRAPPER_SHUTDOWN_PORT;

/**
 * @author Dmitry Ovchinnikov
 */
public class WrapperCli {

    private final CommandLine commandLine;

    public WrapperCli(Properties properties, String... args) throws ParseException {
        commandLine = new PosixParser().parse(getOptions(), args, properties, false);
    }

    private static Options getOptions() {
        return new Options()
                .addOption("h", "help", true, s("Shows this help screen"))
                .addOption("b", "bind", true, s("Bind address [localhost:%d]", DEFAULT_WRAPPER_SHUTDOWN_PORT))
                .addOption("j", "jmx", true, s("Service URL [%s]", DEFAULT_JMX_ADDRESS))
                .addOption("e", "env", true, s("JMX environment"))
                .addOption("f", "forward", true, s("Connector server forwarder"))
                .addOption("n", "instance", true, s("Set instance name"));
    }

    public boolean isHelp() {
        return commandLine.hasOption('h') || commandLine.getArgList().isEmpty();
    }

    public void showHelp() {
        new HelpFormatter().printHelp(80, "java -jar <jar-file> [<options>] start | stop",
                System.lineSeparator() + s("options") + ":",
                getOptions(),
                System.lineSeparator() + "(c) 2014 Marid software development group",
                false);
    }

    public String getInstanceName() {
        return commandLine.getOptionValue('n', "marid-wrapper");
    }

    public InetSocketAddress getBindAddress() {
        final String address = commandLine.getOptionValue('b', "localhost:" + DEFAULT_WRAPPER_SHUTDOWN_PORT);
        try {
            final URI uri = new URI("proto://" + address);
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(address);
        }
    }

    public JMXServiceURL getJmxUrl() throws MalformedURLException {
        return new JMXServiceURL(commandLine.getOptionValue('j', DEFAULT_JMX_ADDRESS));
    }

    public void applyForwarder(JMXConnectorServer server) throws IOException, ReflectiveOperationException {
        final String f = commandLine.getOptionValue('f');
        if (f == null) {
            return;
        }
        if (f.startsWith("@")) {
            server.setMBeanServerForwarder(new MBeanServerFileAccessController(loadProperties(getUrl(f.substring(1)))));
        } else {
            final Class<?> c = Class.forName(f, true, Thread.currentThread().getContextClassLoader());
            server.setMBeanServerForwarder((MBeanServerForwarder) c.newInstance());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> getJmxEnv() throws Exception {
        final String file = commandLine.getOptionValue('e');
        if (file == null) {
            return null;
        } else {
            final URL url = getUrl(file);
            if (url.getFile().endsWith(".properties")) {
                final Properties properties = new Properties();
                try (final Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                    properties.load(r);
                }
                return (Map) properties;
            } else {
                final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                for (final ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
                    for (final String ext : scriptEngineFactory.getExtensions()) {
                        if (url.getFile().endsWith("." + ext)) {
                            try (final Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                                return (Map) scriptEngineFactory.getScriptEngine().eval(r);
                            }
                        }
                    }
                }
                throw new NoSuchFileException(file);
            }
        }
    }

    public String getCommand() {
        return commandLine.getArgs()[0];
    }
}
