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

package org.marid.ide.project.cache;

import com.google.common.collect.ImmutableSet;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.ManualTests;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.nio.file.StandardWatchEventKinds.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({ManualTests.class})
public class WatchKeyTest {

    private Path directory;

    @Before
    public void initTest() throws IOException {
        directory = Files.createTempDirectory(getClass().getSimpleName());
    }

    @After
    public void destroyTest() throws IOException {
        FileUtils.deleteDirectory(directory.toFile());
    }

    @Test
    public void test() throws Exception {
        try (final WatchService watchService = directory.getFileSystem().newWatchService()) {
            directory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            {
                final Path newFile = Files.createFile(directory.resolve("temp.txt"));
                Files.write(newFile, new byte[1]);
                assertTrue(Files.deleteIfExists(newFile));
                final Set<WatchEvent.Kind<?>> kinds = new HashSet<>();
                for (final long time = currentTimeMillis(); currentTimeMillis() - time < 1_000L; ) {
                    final WatchKey key = watchService.poll();
                    if (key == null) {
                        continue;
                    }
                    assertEquals(directory, key.watchable());
                    if (!key.isValid()) {
                        key.cancel();
                    }
                    try {
                        for (final WatchEvent<?> watchEvent : key.pollEvents()) {
                            kinds.add(watchEvent.kind());
                            assertEquals(newFile, directory.resolve((Path) watchEvent.context()));
                        }
                    } finally {
                        key.reset();
                    }
                }
                assertEquals(ImmutableSet.of(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), kinds);
            }
        }
    }
}
