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
package org.marid.marid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Map tests.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MapTest extends Assert {
	
	private static final int SIZE = 100000;
	private static final int THREADS = 100;
	
	private ConcurrentMap<String, Object> map;
	private AtomicLong time;
	private List<String> ids;
	
	@Before
	public void init() {
		time = new AtomicLong(0L);
		ids = new ArrayList<>(SIZE);
		for (int i = 0; i < SIZE; i++) ids.add(UUID.randomUUID().toString());
	}
	
	@Test
	public void test1() throws Exception {
		assertTrue(true);
	}
}
