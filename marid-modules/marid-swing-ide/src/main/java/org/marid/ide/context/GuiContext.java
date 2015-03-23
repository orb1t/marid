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

package org.marid.ide.context;

import org.marid.bd.Block;
import org.marid.ide.base.IdeFrame;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.gui.IdeImpl;
import org.marid.ide.widgets.Widget;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan(basePackageClasses = {IdeImpl.class, Widget.class, MaridFrame.class, Block.class})
public class GuiContext implements LogSupport, SysPrefSupport {

    @Autowired
    private IdeFrame ideFrame;

    @PostConstruct
    public void init() {
        ideFrame.setVisible(true);
    }
}
