package org.marid.proto;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoHealth implements ProtoHealth {

    final AtomicLong successfulTransactionCount = new AtomicLong();
    final AtomicLong failedTransactionCount = new AtomicLong();
    final AtomicLong lastSuccessfulTransactionTimestamp = new AtomicLong();
    final AtomicLong lastFailedTransactionTimestamp = new AtomicLong();

    @Override
    public long getSuccessfulTransactionCount() {
        return successfulTransactionCount.get();
    }

    @Override
    public long getFailedTransactionCount() {
        return failedTransactionCount.get();
    }

    @Override
    public Date getLastSuccessfulTransactionTimestamp() {
        return new Timestamp(lastSuccessfulTransactionTimestamp.get());
    }

    @Override
    public Date getLastFailedTransactionTimestamp() {
        return new Timestamp(lastFailedTransactionTimestamp.get());
    }

    @Override
    public void reset() {
        successfulTransactionCount.set(0L);
        failedTransactionCount.set(0L);
        lastSuccessfulTransactionTimestamp.set(System.currentTimeMillis());
        lastFailedTransactionTimestamp.set(0L);
    }
}
