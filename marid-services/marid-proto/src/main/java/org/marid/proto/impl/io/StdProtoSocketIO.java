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

package org.marid.proto.impl.io;

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
