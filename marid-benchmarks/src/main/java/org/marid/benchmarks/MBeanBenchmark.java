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

package org.marid.benchmarks;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.ThreadLocalRandom.current;
import static javax.management.JMX.newMBeanProxy;

/**
 * @author Dmitry Ovchinnikov.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 3)
@State(Scope.Benchmark)
public class MBeanBenchmark {

    private static final int COUNT = 10_000;

    private static final Some DIRECT = new Some();
    private static final SomeMBean JMX_BEAN;
    private static final ObjectName OBJECT_NAME;
    private static final MBeanServer CONNECTION;

    static {
        try {
            CONNECTION = ManagementFactory.getPlatformMBeanServer();
            OBJECT_NAME = new ObjectName("x:y=z");
            CONNECTION.registerMBean(DIRECT, OBJECT_NAME);
            JMX_BEAN = newMBeanProxy(CONNECTION, OBJECT_NAME, SomeMBean.class);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    private final Blackhole bh = new Blackhole();

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void benchmarkDirect() {
        for (int i = 0; i < COUNT; i++) {
            bh.consume(DIRECT.data(new Data(current().nextLong(), current().nextInt())));
        }
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public void benchmarkJMX() {
        for (int i = 0; i < COUNT; i++) {
            bh.consume(JMX_BEAN.data(new Data(current().nextLong(), current().nextInt())));
        }
    }

    public static void main(String... args) throws Exception {
        new Runner(new OptionsBuilder().include(lookup().lookupClass().getSimpleName()).build()).run();
    }

    public interface SomeMBean {

        Data data(Data data);
    }

    public static class Some implements SomeMBean {

        @Override
        public Data data(Data data) {
            return new Data(data.getX() + 1L, data.getY() + 1);
        }
    }

    public static class Data {

        private final long x;
        private final int y;

        public Data(long x, int y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }
}
