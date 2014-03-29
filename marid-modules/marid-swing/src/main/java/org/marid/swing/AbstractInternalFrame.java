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

package org.marid.swing;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Dmitry Ovchinnikov.
 */
public class AbstractInternalFrame<F extends AbstractMultiFrame> extends InternalFrame<F> {

    private static final Map<AbstractMultiFrame, Integer> COUNTER_MAP = new WeakHashMap<>();

    protected AbstractInternalFrame(F owner, String title) {
        super(owner, title(owner, title), true);
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                putPref("size", getSize());
            }
        });
    }

    private static String title(AbstractMultiFrame owner, String title) {
        final int n = COUNTER_MAP.computeIfAbsent(owner, f -> 0) + 1;
        COUNTER_MAP.put(owner, n);
        return title + " " + n;
    }

    @Override
    public void pack() {
        super.pack();
        setSize(getPref("size", getInitialSize()));
    }
}
