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

package org.marid.logging;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.marid.test.NormalBenchmarks;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.*;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.runners.Parameterized.Parameters;

/**
 * @author Dmitry Ovchinnikov
 */
@RunWith(Parameterized.class)
@Category(NormalBenchmarks.class)
public class LoggingBenchmark extends AbstractBenchmark implements LogSupport {

    private final LogSupport logSupport;

    public LoggingBenchmark(LogSupport logSupport) {
        info("Testing {0} ...", logSupport.getClass().getCanonicalName());
        this.logSupport = logSupport;
        this.logSupport.logger().setUseParentHandlers(false);
        this.logSupport.logger().addHandler(new MemoryHandler(new Handler() {

            private volatile LogRecord logRecord;

            @Override
            public void publish(LogRecord record) {
                logRecord = record;
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }, 128, Level.WARNING));
    }

    @Test
    @BenchmarkOptions(warmupRounds = 1_000, benchmarkRounds = 1_000)
    public void logInfoWithoutParameters() {
        for (int i = 0; i < 1_000; i++) {
            logSupport.info("test");
        }
    }

    @Test
    @BenchmarkOptions(warmupRounds = 1_000, benchmarkRounds = 1_000)
    public void logInfoWithParameters() {
        for (int i = 0; i < 1_000; i++) {
            logSupport.info("test {0} {1}", current().nextDouble(), current().nextDouble());
        }
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{new WithDirectStaticLogger()},
                new Object[]{new WithLogSupport()},
                new Object[]{new WithLocalLogger()}
        );
    }

    static class WithDirectStaticLogger implements LogSupport {

        private static final Logger LOGGER = Logger.getLogger(WithDirectStaticLogger.class.getName());

        @Override
        public Logger logger() {
            return LOGGER;
        }
    }

    static class WithLogSupport implements LogSupport {
    }

    static class WithLocalLogger implements LogSupport {

        private final Logger logger = Logger.getLogger(getClass().getName());

        @Override
        public Logger logger() {
            return logger;
        }
    }
}
