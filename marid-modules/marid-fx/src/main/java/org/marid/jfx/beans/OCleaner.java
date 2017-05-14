/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.jfx.beans;

import javafx.application.Platform;
import org.marid.concurrent.MaridTimerTask;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Dmitry Ovchinnikov
 */
class OCleaner {

    private static final ConcurrentLinkedQueue<WeakReference<OCleanable>> CLEANABLES = new ConcurrentLinkedQueue<>();
    private static final Timer TIMER = new Timer(true);

    static {
        TIMER.schedule(new MaridTimerTask(t -> CLEANABLES.removeIf(ref -> {
            final OCleanable c = ref.get();
            if (c == null) {
                return true;
            } else {
                Platform.runLater(c::clean);
                return false;
            }
        })), 60_000L, 60_000L);
    }

    public static void register(OCleanable cleanable) {
        CLEANABLES.add(new WeakReference<>(cleanable));
    }
}
