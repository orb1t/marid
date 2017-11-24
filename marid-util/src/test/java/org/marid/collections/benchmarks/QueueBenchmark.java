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

package org.marid.collections.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@OperationsPerInvocation(QueueBenchmark.OPERATIONS)
@Measurement(iterations = 5)
@Warmup(iterations = 5)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
public class QueueBenchmark {

  static final int OPERATIONS = 1_000;

  private static final BigInteger[] ARRAY = new Random(0L)
      .longs(OPERATIONS)
      .mapToObj(BigInteger::valueOf)
      .toArray(BigInteger[]::new);

  @Benchmark
  public Collection<BigInteger> add(AddState addState) {
    final Collection<BigInteger> collection = addState.queueType.collectionSupplier.get();
    final Consumer<BigInteger> consumer = collection::add;
    for (final BigInteger v : ARRAY) {
      consumer.accept(v);
    }
    return collection;
  }

  @State(Scope.Benchmark)
  public static class AddState {

    @Param
    private QueueType queueType;
  }

  public enum QueueType {

    LINKEDBLOCKED(LinkedBlockingQueue::new),
    TRANSFER(LinkedTransferQueue::new),
    LINKEDLIST(LinkedList::new),
    CONCURRENT(ConcurrentLinkedQueue::new);

    private final Supplier<Collection<BigInteger>> collectionSupplier;

    QueueType(Supplier<Collection<BigInteger>> collectionSupplier) {
      this.collectionSupplier = collectionSupplier;
    }
  }

  public static void main(String... args) throws Exception {
    new Runner(new OptionsBuilder()
        .include(QueueBenchmark.class.getName())
        .addProfiler(GCProfiler.class)
        .build()
    ).run();
  }
}
