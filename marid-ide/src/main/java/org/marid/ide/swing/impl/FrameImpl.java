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
package org.marid.ide.swing.impl;

import org.marid.ide.itf.Frame;
import org.marid.ide.menu.MenuEntry;
import org.marid.image.MaridIcons;
import org.marid.swing.MaridFrame;

import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

import static org.marid.methods.GuiMethods.getDimension;
import static org.marid.methods.GuiMethods.preferences;

/**
 * Application frame implementation.
 *
 * @author Dmitry Ovchinnikov 
 */
public class FrameImpl extends MaridFrame implements Frame {

    private static final long serialVersionUID = -5821982526972419542L;
    private final ApplicationImpl application;
    private final DesktopImpl desktop;
    private final Preferences prefs = preferences("frame");

    FrameImpl(ApplicationImpl app, List<MenuEntry> menuEntries) {
        super(S.l("Marid IDE"));
        application = app;
        setIconImages(MaridIcons.ICONS);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(desktop = new DesktopImpl());
        setJMenuBar(new MenuBarImpl(menuEntries));
        setPreferredSize(getDimension(prefs, "preferredSize", new Dimension(750, 550)));
        pack();
    }

    @Override
    public DesktopImpl getDesktop() {
        return desktop;
    }

    @Override
    protected String prefNode() {
        return "frame";
    }
}
