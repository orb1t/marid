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

package org.marid;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Dmitry Ovchinnikov
 */
@State(Scope.Thread)
@Warmup(iterations = 10)
@BenchmarkMode(Mode.Throughput)
@Measurement(time = 1, iterations = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(warmups = 1, value = 1)
public abstract class AbstractMapBenchmark<K, V> {

    public static final int COUNT = 1000;

    protected final Map<K, V> map;

    public AbstractMapBenchmark(Map<K, V> map) {
        this.map = map;
    }

    protected abstract K key(int index);

    protected abstract V value(int index);

    @Setup(Level.Invocation)
    @Group("gen")
    public void setupGen() {
        map.clear();
    }

    @Benchmark
    @Group("gen")
    @OperationsPerInvocation(COUNT)
    public void generate() {
        for (int i = 0; i < COUNT; i++) {
            map.put(key(i), value(i));
        }
    }

    @Setup(Level.Trial)
    @Group("retrieve")
    public void setupRetrieve() {
        map.putAll(IntStream.range(0, COUNT).mapToObj(i -> i).collect(Collectors.toMap(this::key, this::value)));
    }

    @Benchmark
    @Group("retrieve")
    @OperationsPerInvocation(COUNT)
    public void retrieve(Blackhole blackhole) {
        for (int i = 0; i < COUNT; i++) {
            blackhole.consume(map.get(key(i)));
        }
    }
}
