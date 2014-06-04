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

package org.marid.bd;

import org.marid.bd.schema.SchemaEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public interface BlockComponent {

    default JComponent getComponent() {
        return (JComponent) this;
    }

    Rectangle getBounds();

    void setBounds(Rectangle rectangle);

    Dimension getPreferredSize();

    boolean isVisible();

    void setVisible(boolean visible);

    default SchemaEditor getSchemaEditor() {
        return (SchemaEditor) getComponent().getParent();
    }

    Block getBlock();

    default Input inputFor(Block.Input<?> input) {
        return getInputs().stream().filter(i -> i.getInput() == input).findFirst().get();
    }

    default Output outputFor(Block.Output<?> output) {
        return getOutputs().stream().filter(o -> o.getOutput() == output).findFirst().get();
    }

    List<Input> getInputs();

    List<Output> getOutputs();

    interface Port {

        BlockComponent getBlockComponent();

        default AbstractButton getButton() {
            return (AbstractButton) this;
        }
    }

    interface Input extends Port {

        Block.Input<?> getInput();
    }

    interface Output extends Port {

        Block.Output<?> getOutput();
    }
}
