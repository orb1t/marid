/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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
package org.marid.db;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Objects;

/**
 * Database access code.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public final class DbAccessCode {

    private final BitSet code;

    /**
     * Constructs the database access code.
     * @param c Access code.
     */
    public DbAccessCode(BitSet c) {
        code = c;
    }

    /**
     * Constructs the database access code.
     * @param data Binary code data.
     */
    public DbAccessCode(byte[] data) {
        code = BitSet.valueOf(data);
    }

    /**
     * Constructs the database access code.
     * @param data Binary code data.
     */
    public DbAccessCode(ByteBuffer data) {
        code = BitSet.valueOf(data);
    }

    /**
     * Checks if read is enabled.
     * @return Read-enabled flag.
     */
    public boolean isReadEnabled() {
        return code.get(0);
    }

    /**
     * Checks if write is enabled.
     * @return Write-enabled flag.
     */
    public boolean isWriteEnabled() {
        return code.get(1);
    }

    /**
     * Checks if delete is enabled.
     * @return Delete-enabled flag.
     */
    public boolean isDeleteEnabled() {
        return code.get(2);
    }

    /**
     * Checks if purge is enabled.
     * @return Purge-enabled flag.
     */
    public boolean isPurgeEnabled() {
        return code.get(3);
    }

    /**
     * Get the binary representation.
     * @return Bytes array.
     */
    public byte[] getBytes() {
        return code.toByteArray();
    }

    /**
     * Get the binary representation.
     * @return Byte buffer.
     */
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(code.toByteArray());
    }

    public DbAccessCode merge(DbAccessCode... codes) {
        BitSet bs = BitSet.valueOf(code.toByteArray());
        for (DbAccessCode c : codes) {
            bs.and(c.code);
        }
        return new DbAccessCode(bs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof DbAccessCode) {
            return code.equals(((DbAccessCode)obj).code);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('r');
        sb.append(isReadEnabled() ? '+' : '-');
        sb.append('w');
        sb.append(isWriteEnabled() ? '+' : '-');
        sb.append('d');
        sb.append(isDeleteEnabled() ? '+' : '-');
        sb.append('p');
        sb.append(isPurgeEnabled() ? '+' : '-');
        return sb.toString();
    }
}
