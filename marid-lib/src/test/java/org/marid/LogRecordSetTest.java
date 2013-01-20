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
package org.marid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.naming.CompositeName;
import junit.framework.TestCase;
import org.marid.db.data.LogRecordSet;

/**
 * Miscellaneous test.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class LogRecordSetTest extends TestCase {

    private LogRecordSet recordSet;

    @Override
    protected void setUp() throws Exception {
        LogRecord lr1 = new LogRecord(Level.INFO, "msg");
        lr1.setParameters(new Object[] {
            null,
            10,
            1L,
            new CompositeName("a/b"),
            new ArrayList<>(Arrays.<Object>asList(1, "x")),
            new Date(10000000L),
            null,
            new Timestamp(100000000L)
        });
        lr1.setThrown(new IllegalAccessException("Illegal access"));
        LogRecord lr2 = new LogRecord(Level.WARNING, null);
        lr2.setThrown(new IllegalArgumentException("msg"));
        recordSet = new LogRecordSet(lr1, lr2);
    }

    @Override
    protected void tearDown() throws Exception {
        recordSet = null;
    }

    public void testForRestorability() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(recordSet);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        LogRecordSet lrs;
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            lrs = (LogRecordSet)ois.readObject();
        }
        assertEquals(recordSet, lrs);
        assertEquals(recordSet.hashCode(), lrs.hashCode());
    }
}
