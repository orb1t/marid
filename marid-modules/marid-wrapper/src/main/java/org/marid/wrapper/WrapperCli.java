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

import org.apache.commons.cli.*;
import org.marid.bind.JaxbUtil;
import org.marid.wrapper.hsqldb.HsqldbConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Properties;

import static org.marid.l10n.L10n.s;
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
                .addOption("c", "configuration", true, s("Set database configuration file"))
                .addOption("s", "settings", true, s("Set settings.sql alternative URL"))
                .addOption("h", "help", true, s("Shows this help screen"))
                .addOption("b", "bind", true, s("Bind address (default: localhost:%d)", DEFAULT_WRAPPER_SHUTDOWN_PORT))
                .addOption("n", "instance-name", true, s("Set instance name"));
    }

    public HsqldbConfiguration configuration() throws IOException {
        return commandLine.hasOption('c')
                ? JaxbUtil.load(HsqldbConfiguration.class, new File(commandLine.getOptionValue('c')))
                : new HsqldbConfiguration();
    }

    public boolean isHelp() {
        return commandLine.hasOption('h') || commandLine.getArgList().isEmpty();
    }

    public void showHelp() {
        new HelpFormatter().printHelp(
                "java -jar <jar-file> [<options>] start | stop",
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

    public URL getSettings() throws MalformedURLException {
        return commandLine.hasOption('s')
                ? new URL(commandLine.getOptionValue('s'))
                : getClass().getResource("settings.sql");
    }

    public String getCommand() {
        return commandLine.getArgs()[0];
    }
}
