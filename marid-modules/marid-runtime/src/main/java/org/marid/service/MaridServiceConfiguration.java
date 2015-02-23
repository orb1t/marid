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

package org.marid.service;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridServiceConfiguration {

    default boolean daemons(MaridService service) {
        return false;
    }

    default long stackSize(MaridService service) {
        return 0L;
    }

    default ThreadFactory threadFactory(MaridService service) {
        final AtomicInteger counter = new AtomicInteger();
        return r -> {
            final String name = "thread-" + counter.getAndIncrement();
            final Thread thread = new Thread(service.threadGroup(), r, name, stackSize(service));
            thread.setDaemon(daemons(service));
            return thread;
        };
    }
}
