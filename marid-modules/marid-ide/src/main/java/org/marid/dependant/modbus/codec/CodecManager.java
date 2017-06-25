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
