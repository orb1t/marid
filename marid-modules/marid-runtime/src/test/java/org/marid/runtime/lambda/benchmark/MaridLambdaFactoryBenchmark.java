/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.lambda.benchmark;

import org.marid.runtime.lambda.MaridLambdaFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleToLongFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import static org.marid.misc.Calls.call;
import static org.marid.misc.Casts.cast;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(iterations = 5)
@Warmup(iterations = 5)
@Fork(value = 1, jvmArgs = {"-XX:+UseG1GC"})
@BenchmarkMode(Mode.AverageTime)
@OperationsPerInvocation(MaridLambdaFactoryBenchmark.COUNT)
public class MaridLambdaFactoryBenchmark {

  public static final int COUNT = 100_000;

  @Benchmark
  public void hardO2P(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.ho2p.applyAsInt(state.strings[i]));
    }
  }

  @Benchmark
  public void proxyO2P(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.po2p.applyAsInt(state.strings[i]));
    }
  }

  @Benchmark
  public void hardP2P(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.hp2p.applyAsLong(state.doubles[i]));
    }
  }

  @Benchmark
  public void proxyP2P(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.pp2p.applyAsLong(state.doubles[i]));
    }
  }

  @Benchmark
  public void hardO2O(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.ho2o.apply(state.strings[i]));
    }
  }

  @Benchmark
  public void proxyO2O(GlobalState state, Blackhole blackhole) {
    for (int i = 0; i < COUNT; i++) {
      blackhole.consume(state.po2o.apply(state.strings[i]));
    }
  }

  @State(Scope.Benchmark)
  public static class GlobalState {

    private final Random random = new Random(0L);
    private final String[] strings = IntStream.range(0, COUNT)
        .mapToObj(i -> new byte[random.nextInt(100) + 1])
        .peek(random::nextBytes)
        .map(a -> new String(a, StandardCharsets.ISO_8859_1))
        .toArray(String[]::new);
    private final double[] doubles = IntStream.range(0, COUNT)
        .mapToDouble(i -> random.nextDouble())
        .toArray();
    private final ToIntFunction<String> ho2p = String::length;
    private final ToIntFunction<String> po2p = cast(call(() -> MaridLambdaFactory.lambda(
        ToIntFunction.class, String.class.getMethod("length")))
    );
    private final DoubleToLongFunction hp2p = Double::doubleToLongBits;
    private final DoubleToLongFunction pp2p = call(() -> MaridLambdaFactory.lambda(
        DoubleToLongFunction.class, Double.class.getMethod("doubleToLongBits", double.class))
    );
    private final Function<String, String> ho2o = String::trim;
    private final Function<String, String> po2o = cast(call(() -> MaridLambdaFactory.lambda(
        Function.class, String.class.getMethod("trim")))
    );
  }

  public static void main(String... args) throws Exception {
    new Runner(new OptionsBuilder()
        .build()
    ).run();
  }
}
