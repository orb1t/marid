package org.marid.dependant.modbus.codec;

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

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class FloatCodec extends ModbusCodec<Float> {

    @NotNull
    @Override
    public String getName() {
        return "Float";
    }

    @NotNull
    @Override
    public byte[] encode(@NotNull Float value) {
        return Ints.toByteArray(Float.floatToIntBits(value));
    }

    @NotNull
    @Override
    public Float decode(@NotNull byte[] value) {
        return Float.intBitsToFloat(Ints.fromByteArray(value));
    }

    @Override
    public int getSize() {
        return 4;
    }
}
