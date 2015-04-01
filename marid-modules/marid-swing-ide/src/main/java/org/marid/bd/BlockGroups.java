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

package org.marid.bd;

import images.Images;
import org.marid.logging.LogSupport;
import org.marid.util.StringUtils;
import org.marid.util.Utils;

import javax.swing.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class BlockGroups {

    private static final Map<String, BlockGroup> BLOCK_GROUP_MAP = new ConcurrentSkipListMap<>();

    public static BlockGroup blockGroup(String id) {
        return BLOCK_GROUP_MAP.computeIfAbsent(id, BlockGroup::new);
    }

    public static class BlockGroup implements LogSupport {

        public final String id;
        public final ImageIcon icon;
        public final String name;

        public BlockGroup(String id) {
            this.id = id;
            final URL url = Utils.currentClassLoader().getResource("blocks/groups/" + id + ".properties");
            final Properties properties = new Properties();
            if (url != null) {
                try {
                    try (final Reader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                        properties.load(r);
                    }
                } catch (Exception x) {
                    info("Unable to load properties for {0}", x, id);
                }
            }
            this.icon = Images.getIcon(properties.getProperty("icon", "block/" + id + ".png"), 22);
            this.name = properties.getProperty("name", StringUtils.capitalize(id));
        }
    }
}
