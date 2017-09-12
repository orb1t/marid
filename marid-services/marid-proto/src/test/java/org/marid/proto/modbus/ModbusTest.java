/*-
 * #%L
 * marid-proto
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

package org.marid.proto.modbus;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.io.IOSupplier;
import org.marid.proto.StdProtoBus;
import org.marid.proto.StdProtoBusProps;
import org.marid.proto.StdProtoRoot;
import org.marid.proto.io.ProtoIO;
import org.marid.proto.io.StdProtoSocketIO;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static java.net.InetAddress.getLocalHost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Tag("slow")
public class ModbusTest {

    private static final SimpleProcessImage IMAGE = new SimpleProcessImage();
    private static final ModbusTCPListener LISTENER = new ModbusTCPListener(3);

    private static int port;

    @BeforeAll
    public static void init() throws Exception {
        //System.setProperty("net.wimpi.modbus.debug", "true");

        IMAGE.addRegister(new SimpleRegister(6890));
        IMAGE.addRegister(new SimpleRegister(8890));

        ModbusCoupler.getReference().setUnitID(1);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setProcessImage(IMAGE);

        LISTENER.setPort(0);
        LISTENER.start();

        final Field serverSocketField = ModbusTCPListener.class.getDeclaredField("m_ServerSocket");
        serverSocketField.setAccessible(true);

        ServerSocket serverSocket = null;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            serverSocket = (ServerSocket) serverSocketField.get(LISTENER);
            if (serverSocket != null) {
                break;
            }
            Thread.yield();
        }
        if (serverSocket != null) {
            port = serverSocket.getLocalPort();
            log(Level.INFO, "Listening port {0}", port);
        }
    }

    @AfterAll
    public static void stop() throws Exception {
        LISTENER.stop();
    }

    @Test
    public void test() throws Exception {
        try (final StdProtoRoot root = new StdProtoRoot("root", "root")) {
            final StdProtoBusProps busProps = new StdProtoBusProps();
            final IOSupplier<ProtoIO> ioProvider = () -> new StdProtoSocketIO(new Socket(getLocalHost(), port));
            busProps.setIoSupplier(ioProvider);
            final StdProtoBus bus = new StdProtoBus(root, "bus1", "bus1", busProps);
            final ModbusTcpDriverProps modbusTcpDriverProps = new ModbusTcpDriverProps();
            modbusTcpDriverProps.setDelay(0L);
            modbusTcpDriverProps.setPeriod(1L);
            modbusTcpDriverProps.setAddress(0);
            modbusTcpDriverProps.setUnitId(1);
            final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
            final ModbusTcpDriver driver = new ModbusTcpDriver(bus, "drv", "drv", modbusTcpDriverProps);
            driver.setConsumers(Collections.singletonList(queue::add));
            driver.start();
            final byte[] data = queue.poll(10L, TimeUnit.SECONDS);
            if (data == null) {
                throw new TimeoutException();
            }
            assertEquals(2, data.length);
            assertEquals(6890, ByteBuffer.wrap(data).asCharBuffer().charAt(0));
        }
    }
}
