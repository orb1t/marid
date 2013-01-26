/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.marid.util.DateUtil;

/**
 * Date tests.
 *
 * @author d.ovchinnikow at gmail.com
 */
public class DateTest extends Assert {

    @Test
    public void testIsoFullDate() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date std = f.parse("2011-03-02T00:10:59.001+0100");
        assertEquals(std, DateUtil.isoToDate("2011-03-02 00:10:59.001+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59.001+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59.001CET"));
    }

    @Test
    public void testIsoFullDateWithoutTimeZone() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date std = f.parse("2011-03-02T00:10:59.001");
        assertEquals(std, DateUtil.isoToDate("2011-03-02 00:10:59.001"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59.001"));
    }

    @Test
    public void testIsoWithoutMS() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date std = f.parse("2011-03-02T00:10:59+0100");
        assertEquals(std, DateUtil.isoToDate("2011-03-02 00:10:59+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59CET"));
    }

    @Test
    public void testIsoWithoutMSAndTimeZone() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date std = f.parse("2011-03-02T00:10:59");
        assertEquals(std, DateUtil.isoToDate("2011-03-02 00:10:59"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:59"));
    }

    @Test
    public void testIsoHhmm() throws Exception {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        Date std = f.parse("2011-03-02T00:10+0100");
        assertEquals(std, DateUtil.isoToDate("2011-03-02 00:10+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:00+0100"));
        assertEquals(std, DateUtil.isoToDate("2011-03-02T00:10:00.000+0100"));
    }
}
