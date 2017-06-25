package org.marid.proto.modbus;

import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class ModbusTcpDriverProps {

    private int unitId = 255;
    private int func = 0x03;
    private int count = 1;
    private long period = 1L;
    private long delay = 1L;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int address;
    private long timeout = 1_000L;
    private long errorTimeout = 100L;

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getFunc() {
        return func;
    }

    public void setFunc(int func) {
        this.func = func;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getErrorTimeout() {
        return errorTimeout;
    }

    public void setErrorTimeout(long errorTimeout) {
        this.errorTimeout = errorTimeout;
    }
}
