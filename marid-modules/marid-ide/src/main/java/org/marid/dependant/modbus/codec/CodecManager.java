/*-
 * #%L
 * marid-ide
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

package org.marid.dependant.modbus.codec;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.misc.Casts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Repository
public class CodecManager {

  private final ObservableList<ModbusCodec<?>> codecs;

  @Autowired
  public CodecManager(ModbusCodec<?>[] codecs) {
    this.codecs = FXCollections.observableArrayList(codecs);
  }

  public <T> ObservableList<ModbusCodec<T>> getCodecs(Class<T> type) {
    return Casts.cast(codecs.filtered(e -> type.isAssignableFrom(e.getType())));
  }
}
