package org.marid.proto;

import org.marid.io.IOBiConsumer;
import org.marid.io.IOBiFunction;
import org.marid.proto.io.ProtoIO;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoBusTaskRunner<T extends ProtoBus> {

    Future<?> runAsync(IOBiConsumer<T, ProtoIO> consumer);

    <R> Future<R> callAsync(IOBiFunction<T, ProtoIO, R> function);

    ScheduledFuture<?> schedule(IOBiConsumer<T, ProtoIO> task, long delay, long period, TimeUnit unit, boolean fair);

    void run(IOBiConsumer<T, ProtoIO> consumer);

    <R> R call(IOBiFunction<T, ProtoIO, R> function);
}
