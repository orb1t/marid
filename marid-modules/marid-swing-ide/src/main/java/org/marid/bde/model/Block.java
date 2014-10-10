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

package org.marid.bde.model;

import images.Images;
import org.marid.dyn.MetaInfo;
import org.marid.itf.Named;
import org.marid.l10n.L10nSupport;
import org.marid.swing.dnd.DndObject;

import javax.swing.*;
import java.io.Serializable;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo
public abstract class Block implements Named, DndObject, L10nSupport {

    public abstract Object getObject();

    public abstract Param[] getParameters();

    public abstract In[] getInputs();

    public abstract Out[] getOutputs();

    public boolean hasParameters() {
        return getParameters().length > 0;
    }

    public boolean hasOutputs() {
        return getOutputs().length > 0;
    }

    public boolean hasInputs() {
        return getInputs().length > 0;
    }

    @Override
    public ImageIcon getVisualRepresentation() {
        return Images.getIcon(getMetaInfo().icon());
    }

    public MetaInfo getMetaInfo() {
        final MetaInfo metaInfo = getType().getAnnotation(MetaInfo.class);
        return metaInfo == null ? Block.class.getAnnotation(MetaInfo.class) : metaInfo;
    }

    public Out getSelfOutput() {
        return new Out() {
            @Override
            public boolean isSelf() {
                return true;
            }

            @Override
            public Class<?> getType() {
                return Block.this.getType();
            }

            @Override
            public String getName() {
                return Block.this.getName();
            }
        };
    }

    public abstract Class<?> getType();

    @Override
    public String toString() {
        return s(getName());
    }

    public abstract class Port implements Named, Serializable {

        public Block getBlock() {
            return Block.this;
        }

        public abstract Class<?> getType();

        public MetaInfo getMetaInfo() {
            final MetaInfo metaInfo = getType().getAnnotation(MetaInfo.class);
            return metaInfo == null ? getClass().getAnnotation(MetaInfo.class) : metaInfo;
        }

        @Override
        public String toString() {
            return s(getName());
        }
    }

    @MetaInfo(icon = "block/in.png")
    public abstract class In extends Port {

        private Out out;

        public void setOut(Out out) {
            this.out = out;
        }

        public Out getOut() {
            return out;
        }
    }

    @MetaInfo(icon = "block/param.png")
    public abstract class Param extends In {

    }

    @MetaInfo(icon = "block/out.png")
    public abstract class Out extends Port {

        private In in;

        public boolean isSelf() {
            return false;
        }

        public void setIn(In in) {
            this.in = in;
        }

        public In getIn() {
            return in;
        }
    }
}
