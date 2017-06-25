package org.marid.dependant.modbus.codec;

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
