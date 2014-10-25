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

package org.marid.service;

import org.marid.enums.BlockingQueueType;
import org.marid.enums.RejectionHandlerType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ServiceParameters {

    int queueSize() default 0;

    BlockingQueueType queueType() default BlockingQueueType.SYNCHRONOUS;

    int threads() default 0;

    int maxThreads() default 8;

    long keepAliveTime() default 0L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    int stackSize() default 0;

    boolean daemons() default false;

    boolean poolDaemons() default false;

    long timeGranularity() default 100L;

    long shutdownTimeout() default 10_000L;

    RejectionHandlerType rejectionType() default RejectionHandlerType.CALLER_RUNS;
}
