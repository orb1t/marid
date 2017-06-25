package org.marid.db.hsqldb;

import org.marid.db.dao.NumericReader;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Dmitry Ovchinnikov.
 */
public class HsqldbDaqNumericReader extends HsqldbDaqAbstractReader<Double> implements NumericReader {

    public HsqldbDaqNumericReader(DataSource dataSource, String table) {
        super(dataSource, table);
    }

    @Override
    protected void setValue(PreparedStatement statement, int index, Double value) throws SQLException {
        statement.setDouble(index, value);
    }

    @Override
    protected Double getValue(ResultSet resultSet, int index) throws SQLException {
        return resultSet.getDouble(index);
    }

    @Override
    protected byte[] toByteArray(@Nonnull Double value) {
        return ByteBuffer.allocate(8).putDouble(0, value).array();
    }

    @Override
    protected String getSqlTypeName() {
        return "double";
    }
}
