/*-
 * #%L
 * marid-api
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
