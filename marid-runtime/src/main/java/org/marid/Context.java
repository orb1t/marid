/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.marid.io.EmptyPrintStream;

/**
 * Marid application context.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Context {

    /**
     * Shutdown port.
     */
    public static final int SHUTDOWN_PORT;
    /**
     * Application identifier.
     */
    public static final String APP_ID;
    /**
     * Localhost only shutdown filter flag.
     */
    public static final boolean LOSFF;

    private static int getShutdownPort(Properties p, List<Exception> exs) {
        try {
            return Integer.parseInt(p.getProperty("shutdownPort", "10090"));
        } catch (Exception x) {
            exs.add(new IllegalStateException("Shutdown port", x));
            return 10090;
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    private static PrintStream ps(Properties p, List<Exception> xs, String n) {
        PrintStream ps = null;
        if (p.containsKey("outDir")) {
            try {
                Path f = Paths.get(p.getProperty("outDir"));
                if (!Files.isDirectory(f)) {
                    f = Files.createDirectories(f);
                }
                f = f.resolve(n);
                ps = new PrintStream(Files.newOutputStream(f), true, "UTF-8");
            } catch (Exception x) {
                xs.add(new IllegalStateException("Setting " + n, x));
            }
        }
        if (ps != null) {
            return ps;
        }
        URL url = Context.class.getResource("/log.properties");
        if (url != null && "file".equals(url.getProtocol())) {
            try {
                Path f = Paths.get(url.toURI()).getParent().resolve(n);
                ps = new PrintStream(Files.newOutputStream(f), true, "UTF-8");
            } catch (Exception x) {
                xs.add(new IllegalStateException("Set " + n + " to jarDir", x));
            }
        }
        if (ps != null) {
            return ps;
        }
        try {
            String userHome = System.getProperty("user.home");
            Path f = Paths.get(userHome, "marid");
            if (!Files.isDirectory(f)) {
                f = Files.createDirectory(f);
            }
            f = f.resolve(n);
            ps = new PrintStream(Files.newOutputStream(f), true, "UTF-8");
        } catch (Exception x) {
            xs.add(new IllegalStateException("Setting " + n + " to home", x));
        }
        if (ps != null) {
            return ps;
        } else {
            xs.add(new IllegalStateException("Empty print stream: " + n));
            return new EmptyPrintStream();
        }
    }

    static {
        ArrayList<Exception> exs = new ArrayList<>();
        Properties props = new Properties();
        String propsName = "/marid.properties";
        try (InputStream is = Context.class.getResourceAsStream(propsName)) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception x) {
            exs.add(new IllegalStateException("Marid properties", x));
        }
        SHUTDOWN_PORT = getShutdownPort(props, exs);
        APP_ID = props.getProperty("appid", "marid");
        LOSFF = "true".equals(props.getProperty("losff"));
        System.setOut(ps(props, exs, "marid.log"));
        System.setErr(ps(props, exs, "marid.err"));
        for (Exception x : exs) {
            x.printStackTrace(System.err);
        }
        exs.clear();
    }
}
