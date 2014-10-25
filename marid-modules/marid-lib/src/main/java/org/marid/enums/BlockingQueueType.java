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

package org.marid.enums;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.IntFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public enum BlockingQueueType implements IntFunction<BlockingQueue<Runnable>> {

    SYNCHRONOUS(p -> new SynchronousQueue<>()),
    LINKED(p -> new LinkedBlockingQueue<>(p <= 0 ? Integer.MAX_VALUE : p)),
    ARRAY(p -> new LinkedBlockingQueue<>(p <= 0 ? 1024 : p)),
    PRIORITY(p -> new PriorityBlockingQueue<>());

    private final IntFunction<BlockingQueue<Runnable>> function;

    private BlockingQueueType(IntFunction<BlockingQueue<Runnable>> function) {
        this.function = function;
    }

    @Override
    public BlockingQueue<Runnable> apply(int queueSize) {
        return function.apply(queueSize);
    }
}
