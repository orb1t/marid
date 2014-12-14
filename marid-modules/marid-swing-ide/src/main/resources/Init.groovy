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

import org.marid.bd.Block
import org.marid.ide.components.ProfileManager
import org.marid.ide.frames.MaridFrame
import org.marid.ide.swing.IdeSplashScreen
import org.marid.ide.swing.context.GuiContext
import org.marid.ide.swing.gui.IdeImpl
import org.marid.ide.widgets.Widget
import org.marid.logging.Logging
import org.marid.swing.log.SwingHandler

info("Adding swing handler");
Logging.rootLogger().addHandler(new SwingHandler());

info("Handling splash screen");
IdeSplashScreen.start();

info("Scanning packages");
maridContext.scan(
        GuiContext.package.name,
        IdeImpl.package.name,
        ProfileManager.package.name,
        Block.package.name,
        Widget.package.name,
        MaridFrame.package.name);