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
package org.marid.db.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data record set.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class DataRecordSet implements Externalizable {

    private final ArrayList<DataRecord> dataRecordList;

    /**
     * Default constructor.
     */
    public DataRecordSet() {
        this(Collections.<DataRecord>emptyList());
    }

    /**
     * Constructs the data record set.
     * @param records Data records.
     */
    public DataRecordSet(DataRecord... records) {
        this(Arrays.asList(records));
    }

    /**
     * Constructs the data record set.
     * @param dataRecords Initial set.
     */
    public DataRecordSet(List<? extends DataRecord> dataRecords) {
        dataRecordList = new ArrayList<>(dataRecords);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Map<Class<?>, List<DataRecord>> map = new IdentityHashMap<>();
        for (DataRecord r : dataRecordList) {
            Class<?> c = r.getClass();
            if (map.containsKey(c)) {
                List<DataRecord> l = map.get(c);
                l.add(r);
            } else {
                map.put(c, new ArrayList<>(Arrays.asList(r)));
            }
        }
        out.writeInt(map.size());
        for (Map.Entry<Class<?>, List<DataRecord>> e : map.entrySet()) {
            out.writeUTF(e.getKey().getName());
            out.writeInt(e.getValue().size());
            for (DataRecord r : e.getValue()) {
                r.writeExternal(out);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws
            IOException, ClassNotFoundException {
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            String className = in.readUTF();
            Class<?> c;
            try {
                c = Class.forName(className);
            } catch (Exception x) {
                try {
                    c = Class.forName(className, true,
                            Thread.currentThread().getContextClassLoader());
                } catch (Exception y) {
                    throw new ClassNotFoundException(className, x);
                }
            }
            Class<? extends DataRecord> drc = (Class<? extends DataRecord>)c;
            int m = in.readInt();
            for (int k = 0; k < m; k++) {
                DataRecord r;
                try {
                    r = drc.newInstance();
                } catch (IllegalAccessException | InstantiationException x) {
                    throw new IllegalStateException(x);
                }
                r.readExternal(in);
                dataRecordList.add(r);
            }
        }
    }
}
