/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.jmx;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class MBeanTest {

    @Test
    public void testRegister() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("x:y=z");
        final ObjectInstance objectInstance = server.registerMBean(new Some(), objectName);
        try {
            final SomeMBean bean = JMX.newMBeanProxy(server, objectName, SomeMBean.class);
            Assert.assertEquals(1L, bean.getData().getField1());
            Assert.assertEquals(2, bean.getData().getField2());
        } finally {
            server.unregisterMBean(objectInstance.getObjectName());
        }
    }

    public static class Data {

        private final long field1;
        private final int field2;

        public Data(long field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public long getField1() {
            return field1;
        }

        public int getField2() {
            return field2;
        }

        @Override
        public String toString() {
            return String.format("%s(field1=%s,field2=%s)", getClass().getSimpleName(), field1, field2);
        }
    }

    public static class Some implements SomeMBean {
        @Override
        public Data getData() {
            return new Data(1L, 2);
        }
    }

    public interface SomeMBean {

        Data getData();
    }
}
