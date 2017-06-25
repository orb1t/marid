package org.marid.proto;

import java.util.Date;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoHealth {

    long getSuccessfulTransactionCount();

    long getFailedTransactionCount();

    Date getLastSuccessfulTransactionTimestamp();

    Date getLastFailedTransactionTimestamp();

    void reset();
}
