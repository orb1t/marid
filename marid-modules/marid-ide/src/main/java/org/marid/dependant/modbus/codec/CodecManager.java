/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
