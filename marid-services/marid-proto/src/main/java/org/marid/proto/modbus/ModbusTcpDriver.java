package org.marid.proto.modbus;

import org.marid.misc.Casts;
import org.marid.proto.ProtoBus;
import org.marid.proto.ProtoDriver;
import org.marid.proto.StdProto;

import javax.annotation.PostConstruct;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.ByteBuffer.allocate;

/**
 * @author Dmitry Ovchinnikov
 */
public class ModbusTcpDriver extends StdProto implements ProtoDriver {

    private final ProtoBus bus;
    private final char transactionIdentifier;
    private final char slaveAndFunc;
    private final char address;
    private final char count;
    private final long delay;
    private final long period;
    private final TimeUnit timeUnit;
    private final ArrayList<Consumer<byte[]>> consumers = new ArrayList<>();

    private ScheduledFuture<?> task;

    public ModbusTcpDriver(ProtoBus bus, String id, String name, ModbusTcpDriverProps props) {
        super(id, name);
        this.bus = bus;
        this.bus.getItems().put(id, Casts.cast(this));
        this.transactionIdentifier = (char) bus.getItems().size();
        this.slaveAndFunc = allocate(2).put(0, (byte) props.getUnitId()).put(1, (byte) props.getFunc()).getChar(0);
        this.address = (char) props.getAddress();
        this.count = (char) props.getCount();
        this.delay = props.getDelay();
        this.period = props.getPeriod();
        this.timeUnit = props.getTimeUnit();
    }

    @PostConstruct
    @Override
    public synchronized void start() {
        if (task != null) {
            return;
        }
        task = bus.getTaskRunner().schedule((b, ch) -> ch.doWith((is, os) -> {
            final DataOutputStream output = new DataOutputStream(os);
            output.writeChar(transactionIdentifier);
            output.writeChar(0);
            output.writeChar(6);
            output.writeChar(slaveAndFunc);
            output.writeChar(address);
            output.writeChar(count);
            final DataInputStream input = new DataInputStream(is);
            final char ti = input.readChar();
            if (ti != transactionIdentifier) {
                throw new StreamCorruptedException("Invalid transaction identifier: " + (int) ti);
            }
            final char pi = input.readChar();
            if (pi != 0) {
                throw new StreamCorruptedException("Invalid protocol: " + (int) pi);
            }
            final char size = input.readChar();
            if (size != 3 + 2 * count) {
                throw new StreamCorruptedException("Incorrect size: " + (int) size);
            }
            final char saf = input.readChar();
            if (saf != slaveAndFunc) {
                throw new StreamCorruptedException("Incorrect slave and func: " + Integer.toHexString(saf));
            }
            final int n = is.read();
            if (n / 2 != count) {
                throw new StreamCorruptedException("Incorrect bytes count: " + n);
            }
            final byte[] data = new byte[count * 2];
            input.readFully(data);
            consumers.forEach(c -> c.accept(data));
        }), delay, period, timeUnit, false);
    }

    @Override
    public synchronized void close() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return task != null;
    }

    @Override
    public ProtoBus getParent() {
        return bus;
    }

    public void setConsumers(List<Consumer<byte[]>> consumers) {
        this.consumers.clear();
        this.consumers.addAll(consumers);
        this.consumers.trimToSize();
    }

    public List<Consumer<byte[]>> getConsumers() {
        return consumers;
    }

    @Override
    public String toString() {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put("tid", String.format("%04X", (int) transactionIdentifier));
        map.put("sf", String.format("%04X", (int) slaveAndFunc));
        map.put("addr", String.format("%04X", (int) address));
        map.put("count", String.format("%04X", (int) count));
        return super.toString() + map;
    }
}
