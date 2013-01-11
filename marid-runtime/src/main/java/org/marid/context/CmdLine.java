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
package org.marid.context;

import groovyjarjarcommonscli.CommandLine;
import groovyjarjarcommonscli.HelpFormatter;
import groovyjarjarcommonscli.Option;
import groovyjarjarcommonscli.Options;
import groovyjarjarcommonscli.ParseException;
import groovyjarjarcommonscli.Parser;
import groovyjarjarcommonscli.PosixParser;
import java.util.Properties;
import org.marid.l10n.Localized;

/**
 * Command line configuration.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class CmdLine implements Localized {

    private final CommandLine commandLine;
    private final Options options = new Options();

    /**
     * Constructs the command-line configuration.
     * @param props Runtime properties.
     * @param args Command-line arguments.
     * @throws ParseException Parse exception.
     */
    public CmdLine(Properties props, String... args) throws ParseException {
        add(options, "h", "help", false, "Show the command-line help");
        add(options, "r", "redir", false, "Set the output redirection flag");
        add(options, "o", "out", true, "Set the output directory");
        add(options, "m", "logsize", true, "Set the maximal log size");
        add(options, "l", "locale", true, "Set the locale");
        add(options, "z", "timezone", true, "Set the timezone");
        add(options, "j", "jmx", false, "Set the JMX server enable flag");
        add(options, "p", "port", true, "Set the JMX server port");
        add(options, "k", "key", true, "Set the key file");
        add(options, "i", "pswitf", true, "Set the password interface");
        add(options, "u", "user", true, "Set the user name");
        add(options, "v", "version", false, "Show the program version");
        Parser parser = new PosixParser();
        commandLine = parser.parse(options, args, props);
        for (Option o : commandLine.getOptions()) {
            if (!commandLine.hasOption(o.getLongOpt())) {
                continue;
            }
            if (!o.hasArg()) {
                props.setProperty(o.getLongOpt(), Boolean.toString(true));
            } else {
                props.setProperty(o.getLongOpt(), o.getValue());
            }
        }
    }

    /**
     * Checks whether the help flag is set.
     * @return Help flag.
     */
    public boolean isHelp() {
        return commandLine.hasOption("help");
    }

    /**
     * Get the command to execute.
     * @return Command name (e.g. start, stop).
     */
    public String getCommand() {
        String[] args = commandLine.getArgs();
        return args == null || args.length == 0 ? "start" : args[0];
    }

    /**
     * Shows the help screen.
     */
    public void showHelp() {
        System.out.println(S.l("Marid, the free data acquisition software"));
        System.out.println();
        HelpFormatter hf = new HelpFormatter();
        String l = System.getProperty("line.separator");
        String cls = S.l("java -jar marid-runtime.jar <options>");
        String hdr = l + S.l("Options:");
        String ftr = l + S.l("(c) 2012-2013 Marid Software Development Group");
        hf.printHelp(cls, hdr, options, ftr);
    }

    private void add(Options g, String s, String l, boolean p, String d) {
        g.addOption(new Option(s, l, p, S.l(d)));
    }
}
