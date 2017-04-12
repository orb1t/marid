/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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
