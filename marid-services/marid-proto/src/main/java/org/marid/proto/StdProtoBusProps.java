package org.marid.proto;

import org.marid.io.IOSupplier;
import org.marid.proto.io.ProtoIO;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoBusProps {

    private long terminationTimeout = 10_000L;
    private int threadCount = 1;
    private long stackSize;
    private IOSupplier<? extends ProtoIO> ioSupplier;

    public long getTerminationTimeout() {
        return terminationTimeout;
    }

    public void setTerminationTimeout(long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getStackSize() {
        return stackSize;
    }

    public void setStackSize(long stackSize) {
        this.stackSize = stackSize;
    }

    public IOSupplier<? extends ProtoIO> getIoSupplier() {
        return ioSupplier;
    }

    public void setIoSupplier(IOSupplier<? extends ProtoIO> ioSupplier) {
        this.ioSupplier = ioSupplier;
    }
}
