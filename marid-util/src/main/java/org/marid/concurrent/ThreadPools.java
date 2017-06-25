package org.marid.concurrent;

import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface ThreadPools {

    CallerRunsPolicy CALLER_RUNS_POLICY = new CallerRunsPolicy();
    AbortPolicy ABORT_POLICY = new AbortPolicy();
    DiscardPolicy DISCARD_POLICY = new DiscardPolicy();
    DiscardOldestPolicy DISCARD_OLDEST_POLICY = new DiscardOldestPolicy();
}
