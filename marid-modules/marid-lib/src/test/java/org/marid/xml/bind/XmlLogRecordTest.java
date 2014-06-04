/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.xml.bind;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.logging.LogSupport;
import org.marid.test.NormalTests;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Category(NormalTests.class)
public class XmlLogRecordTest implements LogSupport {

    @Test
    public void testWrite() throws Exception {
        final LogRecord logRecord = new LogRecord(Level.INFO, "abc {0}");
        logRecord.setParameters(new Object[]{
                "a",
                1,
                1L,
                0.5,
                0.4f,
                null,
                'a',
                new byte[]{10, 20},
                BigDecimal.ONE,
                BigInteger.ONE,
                new char[]{'a', 'q'},
                new float[]{0.4f},
                new double[]{0.3, 0.8},
                new Date()});
        final StringWriter sw = new StringWriter();
        final XmlLogRecord xmlLogRecord = new XmlLogRecord(logRecord);
        XmlDomain.save(xmlLogRecord, sw, true, true);
    }

    @Test
    public void testReadWriteFragment() throws Exception {
        final LogRecord logRecord = new LogRecord(Level.INFO, "abc {0}");
        logRecord.setParameters(new Object[]{
                "a",
                1,
                1L,
                0.5,
                0.4f,
                null,
                'a',
                new byte[]{10, 20},
                BigDecimal.ONE,
                BigInteger.ONE,
                new char[]{'a', 'q'},
                new float[]{0.4f},
                new double[]{0.3, 0.8},
                new Date()});
        final StringWriter sw = new StringWriter();
        final XmlLogRecord xmlLogRecord = new XmlLogRecord(logRecord);
        XmlDomain.save(xmlLogRecord, sw, true, true);
        final StringReader sr = new StringReader(sw.toString());
        final XmlLogRecord cloned = XmlDomain.load(XmlLogRecord.class, sr);
        Assert.assertEquals(xmlLogRecord, cloned);
    }

    @Test
    public void testInstant() {
        final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        final Instant cloned = Instant.parse(instant.toString());
        Assert.assertEquals(instant, cloned);
    }
}
