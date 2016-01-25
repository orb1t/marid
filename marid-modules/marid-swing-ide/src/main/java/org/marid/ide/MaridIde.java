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

package org.marid.ide;

import org.jboss.logmanager.LogManager;
import org.marid.logging.Logging;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.log.SwingHandler;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde {

    static {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        LogManager.getLogManager().reset();
    }

    public static final Logger LOGGER = Logger.getLogger("marid");
    public static final GenericXmlApplicationContext CONTEXT = new GenericXmlApplicationContext();

    public static void main(String[] args) throws Exception {
        Logging.rootLogger().addHandler(new SwingHandler());
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(LOGGER, WARNING, "Uncaught exception in {0}", e, t));
        final String lafName = SysPrefSupport.SYSPREFS.get("laf", NimbusLookAndFeel.class.getName());
        try {
            UIManager.setLookAndFeel(lafName);
        } catch (Exception x) {
            LOGGER.log(WARNING, x, () -> "LookAndFeel setting error: " + lafName);
        }
        if (UIManager.getLookAndFeel() instanceof NimbusLookAndFeel) {
            UIManager.put("Nimbus.keepAlternateRowColor", true);
        }
        EventQueue.invokeLater(() -> {
            CONTEXT.load("classpath*:/META-INF/marid/*.xml");
            CONTEXT.refresh();
            CONTEXT.start();
        });
    }
}
