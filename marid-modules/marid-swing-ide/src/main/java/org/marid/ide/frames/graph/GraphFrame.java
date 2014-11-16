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

package org.marid.ide.frames.graph;

import org.marid.dyn.MetaInfo;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.frames.CloseableFrame;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.profile.Profile;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.jmx.SwingJmxAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Dmitry Ovchinnikov.
 */
@CloseableFrame
@MetaInfo(name = "Graph")
public class GraphFrame extends MaridFrame implements DndTarget<SwingJmxAttribute> {

    private final Profile profile;

    @Autowired
    public GraphFrame(GenericApplicationContext context) {
        super(context, LS.s("Graph"));
        profile = context.getBean(ProfileManager.class).getCurrentProfile();
        setTransferHandler(new MaridTransferHandler());
    }

    @Override
    public int getTargetDndActionsSupported() {
        return DND_LINK;
    }

    @Override
    protected void fillActions() {

    }
}
