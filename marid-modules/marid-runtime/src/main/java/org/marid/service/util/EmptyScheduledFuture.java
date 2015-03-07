/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.service.util;

import org.marid.util.Utils;

import javax.annotation.Nonnull;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class EmptyScheduledFuture implements ScheduledFuture<Object> {

    private static final EmptyScheduledFuture INSTANCE = new EmptyScheduledFuture();

    public static <V> ScheduledFuture<V> getInstance() {
        return Utils.cast(INSTANCE);
    }

    private EmptyScheduledFuture() {
    }

    @Override
    public long getDelay(@Nonnull TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(@Nonnull Delayed o) {
        return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, @Nonnull TimeUnit unit) {
        return null;
    }
}
