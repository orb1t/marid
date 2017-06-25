/*
 *
 */

package org.marid.concurrent;

/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import javax.annotation.Nonnull;
import java.util.TimerTask;
import java.util.function.Consumer;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class MaridTimerTask extends TimerTask {

    private final Consumer<MaridTimerTask> task;

    public MaridTimerTask(@Nonnull Consumer<MaridTimerTask> task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.accept(this);
        } catch (RuntimeException x) {
            log(WARNING, "Timer task error", x);
        }
    }
}
