/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utils {

    public static void callWithTime(TimeUnit timeUnit, Runnable task, LongConsumer timeConsumer) {
        final long startTime = System.nanoTime();
        task.run();
        final long time = System.nanoTime() - startTime;
        timeConsumer.accept(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }

    public static void callWithTime(Runnable task, LongConsumer timeConsumer) {
        callWithTime(TimeUnit.MILLISECONDS, task, timeConsumer);
    }

    public static <T> T callWithTime(TimeUnit timeUnit, Callable<T> task, BiConsumer<Long, Exception> timeConsumer) {
        final long startTime = System.nanoTime();
        Exception exception = null;
        T result = null;
        try {
            result = task.call();
        } catch (Exception x) {
            exception = x;
        }
        final long time = System.nanoTime() - startTime;
        timeConsumer.accept(timeUnit.convert(time, TimeUnit.NANOSECONDS), exception);
        return result;
    }

    public static <T> T callWithTime(Callable<T> task, BiConsumer<Long, Exception> timeConsumer) {
        return callWithTime(TimeUnit.MILLISECONDS, task, timeConsumer);
    }

    public static void merge(Properties properties, String... resources) throws IOException {
        for (final String resource : resources) {
            try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        }
    }
}
