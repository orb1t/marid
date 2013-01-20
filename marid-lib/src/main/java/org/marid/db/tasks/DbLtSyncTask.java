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
package org.marid.db.tasks;

import java.util.List;
import java.util.Map;
import javax.naming.CompositeName;
import javax.naming.Name;
import org.marid.db.storage.ArchiveStorage;
import org.marid.db.util.IncMap;

/**
 * Database synchronization task (by last timestamps).
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class DbLtSyncTask implements DbTask {

    private final ArchiveStorage lStorage;
    private final ArchiveStorage rStorage;

    /**
     * Constructs the database synchronization task.
     * @param ls Local storage.
     * @param rs Remote storage.
     */
    public DbLtSyncTask(ArchiveStorage ls, ArchiveStorage rs) {
        lStorage = ls;
        rStorage = rs;
    }

    @Override
    public DbTaskResult call() throws Exception {
        long start = System.currentTimeMillis();
        List<Name> localTags = lStorage.getTags(new CompositeName());
        Map<Name, Long> rss = rStorage.getLastSnapshot(localTags);
        int uc = rStorage.insert(lStorage.queryAfter(new IncMap(rss, 1L)));
        return new DbTaskResult(uc, System.currentTimeMillis() - start);
    }
}
