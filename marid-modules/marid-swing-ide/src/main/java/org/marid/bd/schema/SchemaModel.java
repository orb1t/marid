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

package org.marid.bd.schema;

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.BlockLink;

import java.awt.*;
import java.beans.ConstructorProperties;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaModel {

    protected final Schema schema;
    protected final Map<Block, BlockLayoutInfo> blockMap;
    protected final Map<BlockLink, BlockLinkLayoutInfo> blockLinkMap;

    @ConstructorProperties({"schema", "blockMap", "blockLinkMap"})
    public SchemaModel(Schema schema, Map<Block, BlockLayoutInfo> blockMap, Map<BlockLink, BlockLinkLayoutInfo> blockLinkMap) {
        this.schema = schema;
        this.blockMap = blockMap;
        this.blockLinkMap = blockLinkMap;
    }

    public SchemaModel() {
        this(new Schema(), new IdentityHashMap<>(), new IdentityHashMap<>());
    }

    public void addBlock(BlockComponent blockComponent, Point location) {
        schema.addBlock(blockComponent.getBlock());
        blockMap.put(blockComponent.getBlock(), new BlockLayoutInfo(location));
    }

    public void removeBlock(BlockComponent blockComponent) {
        schema.removeBlock(blockComponent.getBlock());
        blockMap.remove(blockComponent.getBlock());
    }

    public void addBlockLink(BlockComponent.Output output, BlockComponent.Input input) {
        schema.addBlockLink(new BlockLink(output.getOutput(), input.getInput()));
    }

    public void removeBlockLink(BlockLink blockLink) {
        schema.removeBlockLink(blockLink);
        blockLinkMap.remove(blockLink);
    }

    public Schema getSchema() {
        return schema;
    }

    public Map<Block, BlockLayoutInfo> getBlockMap() {
        return blockMap;
    }

    public Map<BlockLink, BlockLinkLayoutInfo> getBlockLinkMap() {
        return blockLinkMap;
    }

    public static class BlockLayoutInfo {

        private volatile Point location;

        @ConstructorProperties({"location"})
        public BlockLayoutInfo(Point location) {
            this.location = location;
        }

        public Point getLocation() {
            return location;
        }

        public void setLocation(Point location) {
            this.location = location;
        }
    }

    public static class BlockLinkLayoutInfo {

    }
}
