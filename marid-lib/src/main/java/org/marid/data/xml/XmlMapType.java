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
package org.marid.data.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * XML map type.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
@XmlType
public enum XmlMapType {
	@XmlEnumValue(value="hash-map")
	HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new HashMap<>();
		}
	},
	@XmlEnumValue(value="linked-hash-map")
	LINKED_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new LinkedHashMap<>();
		}
	},
	@XmlEnumValue(value="tree-map")
	TREE_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new TreeMap<>();
		}
	},
	@XmlEnumValue(value="weak-hash-map")
	WEAK_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new WeakHashMap<>();
		}
	},
	@XmlEnumValue(value="concurrent-hash-map")
	CONCURRENT_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new ConcurrentHashMap<>();
		}
	},
	@XmlEnumValue(value="concurrent-skip-list-map")
	CONCURRENT_SKIP_LIST_MAP {
		@Override
		public Map<String, ?> newMap() {
			return new ConcurrentSkipListMap<>();
		}
	},
	@XmlEnumValue(value="sync-hash-map")
	SYNC_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return Collections.synchronizedMap(new HashMap<String, Object>());
		}
	},
	@XmlEnumValue(value="sync-linked-hash-map")
	SYNC_LINKED_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return Collections.synchronizedMap(
					new LinkedHashMap<String, Object>());
		}
	},
	@XmlEnumValue(value="sync-tree-map")
	SYNC_TREE_MAP {
		@Override
		public Map<String, ?> newMap() {
			return Collections.synchronizedSortedMap(
					new TreeMap<String, Object>());
		}
	},
	@XmlEnumValue(value="sync-weak-hash-map")
	SYNC_WEAK_HASH_MAP {
		@Override
		public Map<String, ?> newMap() {
			return Collections.synchronizedMap(
					new WeakHashMap<String, Object>());
		}
	},
	@XmlEnumValue(value="properties")
	PROPERTIES {
		@Override
		public Map<String, ?> newMap() {
			return (Map)new Properties();
		}
	};
	
	public abstract Map<String, ?> newMap();
}
