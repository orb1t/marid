package org.marid.proto.io;

import org.marid.io.IOBiConsumer;
import org.marid.io.IOBiFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class HalfDuplexProtoIO implements ProtoIO {

    private final ProtoIO delegate;

    public HalfDuplexProtoIO(ProtoIO delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream getInputStream() {
        return delegate.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return delegate.getOutputStream();
    }

    @Override
    public void doWith(IOBiConsumer<InputStream, OutputStream> consumer) throws IOException {
        synchronized (delegate) {
            ProtoIO.super.doWith(consumer);
        }
    }

    @Override
    public <T> T call(IOBiFunction<InputStream, OutputStream, T> function) throws IOException {
        synchronized (delegate) {
            return ProtoIO.super.call(function);
        }
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
