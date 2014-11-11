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

package org.marid.ide.frames;

import org.marid.dyn.MetaInfo;
import org.marid.swing.AbstractFrame;
import org.marid.swing.listeners.MaridWindowListener;
import org.springframework.context.support.GenericApplicationContext;

import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class MaridFrame extends AbstractFrame implements MaridWindowListener {

    protected final GenericApplicationContext context;

    public MaridFrame(GenericApplicationContext context, String title, Object... args) {
        super(LS.s(title, args));
        this.context = context;
        addWindowListener(this);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        context.getAutowireCapableBeanFactory().destroyBean(this);
    }
}
