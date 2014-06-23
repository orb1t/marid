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

package org.marid.bde.view.ga;

import org.marid.bde.view.BlockEditor;
import org.marid.bde.view.BlockLink;
import org.marid.bde.view.BlockView;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Dmitry Ovchinnikov.
 */
public class GaContext {

    public final Rectangle[] rectangles;
    public final Point p1;
    public final Point p2;
    public final ThreadLocalRandom random = ThreadLocalRandom.current();

    public GaContext(BlockLink<?> blockLink) {
        synchronized (blockLink.in.getEditor().getTreeLock()) {
            final BlockEditor blockEditor = blockLink.in.getEditor();
            this.rectangles = blockEditor.blockViews.stream().map(BlockView::getSafeBounds).toArray(Rectangle[]::new);
            this.p1 = blockLink.out.connectionPoint();
            this.p2 = blockLink.in.connectionPoint();
        }
    }

    public float getMutationProbability() {
        return 0.01f;
    }
}