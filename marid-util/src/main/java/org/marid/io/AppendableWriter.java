package org.marid.io;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Dmitry Ovchinnikov
 */
public class AppendableWriter extends Writer {

    private final Appendable appendable;

    public AppendableWriter(@Nonnull Appendable appendable) {
        this.appendable = appendable;
    }

    @Override
    public void write(@Nonnull char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            appendable.append(cbuf[i + off]);
        }
    }

    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }
}
