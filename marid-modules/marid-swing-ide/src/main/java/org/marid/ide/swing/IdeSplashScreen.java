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

package org.marid.ide.swing;

import org.marid.ide.base.IdeFrame;
import org.marid.image.MaridIcon;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.logging.Logging;
import org.marid.logging.SimpleHandler;
import org.marid.pref.PrefSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.StandardLookAndFeel;
import org.marid.swing.log.LogComponent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static java.awt.Frame.getFrames;
import static java.util.Arrays.stream;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeSplashScreen extends JWindow implements PrefSupport, L10nSupport, LogSupport, SysPrefSupport {

    private final Timer timer;

    public IdeSplashScreen() {
        getRootPane().setBorder(createLineBorder(SystemColor.controlHighlight, 3, true));
        setType(Type.POPUP);
        setAlwaysOnTop(true);
        final JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBorder(createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel(MaridIcon.getIcon(64, Color.GREEN)), BorderLayout.WEST);
        panel.add(new JLabel(String.format("<html><big><b>Marid</b>, %s</big></html>", s("the free data-acquisition software"))));
        add(panel, BorderLayout.NORTH);
        final LogComponent logComponent = new LogComponent(preferences(), Collections.emptyList(), r -> true);
        logComponent.getTable().removeColumn(logComponent.getTable().getColumnModel().getColumn(2));
        logComponent.getTable().removeColumn(logComponent.getTable().getColumnModel().getColumn(1));
        final Logger rootLogger = Logging.rootLogger();
        final Handler handler = new SimpleHandler((h, r) -> logComponent.publish(r));
        rootLogger.addHandler(handler);
        add(new JScrollPane(logComponent));
        timer = new Timer(1000, event -> {
            final Frame frame = stream(getFrames()).filter(f -> f instanceof IdeFrame).findFirst().orElse(null);
            if (frame != null && ((IdeFrame) frame).isInitialized()) {
                rootLogger.removeHandler(handler);
                dispose();
            }
        });
        timer.start();
        setPreferredSize(new Dimension(750, 550));
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
    }

    public static void start() {
        UIManager.installLookAndFeel("Standard", StandardLookAndFeel.class.getName());
        final String laf = SYSPREFS.get("laf", NimbusLookAndFeel.class.getCanonicalName());
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception x) {
            Log.warning("Unable to set LAF {0}", x, laf);
        }
        if (UIManager.getLookAndFeel() instanceof NimbusLookAndFeel) {
            UIManager.put("Nimbus.keepAlternateRowColor", true);
        }
    }
}
