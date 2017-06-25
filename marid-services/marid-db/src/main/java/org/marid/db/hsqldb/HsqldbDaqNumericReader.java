/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
