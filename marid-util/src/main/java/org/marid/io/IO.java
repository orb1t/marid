/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.io;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;

public class IO implements Closeable {

  private final RandomAccessFile raf;
  private final I is;
  private final O os;

  public IO(@NotNull File file, @NotNull String mode) throws IOException {
    raf = new RandomAccessFile(file, "rw" + mode);
    is = new I();
    os = new O();
  }

  public IO(@NotNull File file) throws IOException {
    this(file, "");
  }

  public IO(@NotNull Path file, @NotNull String mode) throws IOException {
    this(file.toFile(), mode);
  }

  public IO(@NotNull Path file) throws IOException {
    this(file, "");
  }

  public InputStream getInput() {
    return is;
  }

  public OutputStream getOutput() {
    return os;
  }

  public void trim() throws IOException {
    raf.setLength(0L);
  }

  @Override
  public void close() throws IOException {
    raf.close();
  }

  private class I extends InputStream {

    @Override
    public int read() throws IOException {
      return raf.read();
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
      return raf.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
      return raf.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
      final long len = raf.length();
      final long ptr = raf.getFilePointer();
      final long available = len - ptr;
      return available > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) available;
    }
  }

  private class O extends OutputStream {

    @Override
    public void write(int b) throws IOException {
      raf.write(b);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
      raf.write(b);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
      raf.write(b, off, len);
    }
  }
}
