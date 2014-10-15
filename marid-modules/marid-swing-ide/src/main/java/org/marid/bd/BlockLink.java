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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.rmi.server.UID;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement
public class BlockLink {

    @XmlID
    @XmlAttribute
    private final String id = new UID().toString();

    @XmlIDREF
    @XmlAttribute
    public final Block source;

    @XmlIDREF
    @XmlAttribute
    public final Block target;

    @XmlAttribute
    public final String output;

    @XmlAttribute
    public final String input;

    public BlockLink(Block source, Block target, String output, String input) {
        this.source = source;
        this.target = target;
        this.output = output;
        this.input = input;
    }

    public BlockLink(Block.Output output, Block.Input input) {
        this(output.getBlock(), input.getBlock(), output.getName(), input.getName());
    }

    public BlockLink() {
        this(null, null, null, null);
    }

    public boolean matchesOutput(Block.Output output) {
        return output.getBlock() == source && output.getName().equals(this.output);
    }

    public boolean matchesInput(Block.Input input) {
        return input.getBlock() == target && input.getName().equals(this.input);
    }

    public boolean matches(Block.Output output, Block.Input input) {
        return matchesOutput(output) && matchesInput(input);
    }

    public Block.Output getBlockOutput() {
        return source.getOutputs().stream().filter(o -> o.getName().equals(output)).findFirst().get();
    }

    public Block.Input getBlockInput() {
        return target.getInputs().stream().filter(i -> i.getName().equals(input)).findFirst().get();
    }

    public Block getSource() {
        return source;
    }

    public Block getTarget() {
        return target;
    }

    public String getOutput() {
        return output;
    }

    public String getInput() {
        return input;
    }

    public void transferValue() {
        getBlockInput().set(getBlockOutput().get());
    }
}
