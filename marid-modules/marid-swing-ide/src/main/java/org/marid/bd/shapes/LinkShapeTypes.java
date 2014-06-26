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

package org.marid.bd.shapes;

import org.marid.bd.BlockComponent;

/**
 * @author Dmitry Ovchinnikov
 */
public class LinkShapeTypes {

    public static final LinkShapeType<Object> LINE = new LinkShapeType<Object>() {
        @Override
        public Object getConfiguration() {
            return null;
        }

        @Override
        public String toString() {
            return "Simple line link";
        }

        @Override
        public LinkShape linkShapeFor(BlockComponent.Output output, BlockComponent.Input input) {
            return new LineLinkShape(output, input);
        }
    };

    public static final LinkShapeType<Object> ORTHO = new LinkShapeType<Object>() {
        @Override
        public Object getConfiguration() {
            return null;
        }

        @Override
        public String toString() {
            return "Orthogonal link";
        }

        @Override
        public LinkShape linkShapeFor(BlockComponent.Output output, BlockComponent.Input input) {
            return new OrthoLinkShape(output, input);
        }
    };

    public static final LinkShapeType<Object> LIVE = new LinkShapeType<Object>() {
        @Override
        public Object getConfiguration() {
            return null;
        }

        @Override
        public String toString() {
            return "Live link";
        }

        @Override
        public LinkShape linkShapeFor(BlockComponent.Output output, BlockComponent.Input input) {
            return new LiveLinkShape(output, input);
        }
    };

    public static abstract class LinkShapeType<C> {

        public abstract C getConfiguration();

        @Override
        public abstract String toString();

        public abstract LinkShape linkShapeFor(BlockComponent.Output output, BlockComponent.Input input);
    }
}
