package org.marid.proto.io;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoSocketIO extends StdProtoIO {

    private final Socket socket;

    public StdProtoSocketIO(Socket socket) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream());
        this.socket = socket;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            socket.close();
        }
    }
}
