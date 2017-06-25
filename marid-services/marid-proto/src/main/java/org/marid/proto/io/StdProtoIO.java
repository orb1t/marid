package org.marid.proto.io;

import java.io.*;
import java.util.logging.Level;

import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoIO implements ProtoIO, Closeable {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public StdProtoIO(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void close() throws IOException {
        try (final InputStream is = inputStream; final OutputStream os = outputStream) {
            log(Level.CONFIG, "Closing {0} and {1}", is, os);
        }
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
