package org.marid.dependant.modbus.repo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Repository;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Repository
public class ModbusConfig {

    public final StringProperty host = new SimpleStringProperty("0.0.0.0");
    public final IntegerProperty port = new SimpleIntegerProperty(10502);
}
