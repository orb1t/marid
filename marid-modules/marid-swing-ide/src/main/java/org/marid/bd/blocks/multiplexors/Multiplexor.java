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

package org.marid.bd.blocks.multiplexors;

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.ConfigurableBlock;
import org.marid.bd.StandardBlock;
import org.marid.bd.components.AbstractBlockComponentEditor;
import org.marid.bd.shapes.Link;
import org.marid.util.CollectionUtils;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Dmitry Ovchinnikov
 */
public class Multiplexor<E> extends StandardBlock implements ConfigurableBlock {

    protected int inputCount;
    protected E[] array;

    protected final Class<E> type;
    protected final List<Input> inputs = new ArrayList<>();
    protected final Output out;
    protected final String iconText;

    public Multiplexor(String name, String iconText, String label, Class<E> type, int inputCount) {
        this.type = type;
        this.out = new MultiplexorOutput();
        this.iconText = iconText;
        this.array = CollectionUtils.getArrayFunction(type).apply(0);
        setInputCount(inputCount);
    }

    @Override
    public String getName() {
        return type.getSimpleName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(out);
    }

    @XmlAttribute
    public int getInputCount() {
        return inputCount;
    }

    public String getIconText() {
        return iconText;
    }

    public Class<E> getType() {
        return type;
    }

    @Override
    public BlockComponent createComponent() {
        final BlockComponent component = super.createComponent();
        addEventListener(component, (MultiplexorListener) v -> EventQueue.invokeLater(() -> {
            final List<Link> links = component.getSchemaEditor().removeAllLinks(component);
            setInputCount(v);
            component.updateBlock();
            component.getSchemaEditor().createLinks(links);
            component.getSchemaEditor().validate();
        }));
        return component;
    }

    @Override
    public void reset() {
        Arrays.fill(array, null);
    }

    public void setInputCount(int newValue) {
        fire(MultiplexorListener.class, () -> inputCount, n -> {
            inputCount = n;
            array = Arrays.copyOf(array, inputCount);
            inputs.clear();
            IntStream.range(0, n).forEach(i -> inputs.add(new In(Integer.toString(i + 1), type, v -> array[i] = v)));
        }, newValue, MultiplexorListener::inputCountChanged);
    }

    @Override
    public Window createWindow(Window parent) {
        return new MultiplexorEditor(parent);
    }

    protected class MultiplexorOutput implements Output {

        @Override
        public E[] get() {
            return Arrays.stream(array).filter(e -> e != null).toArray(CollectionUtils.getArrayFunction(type));
        }

        @Override
        public Class<E[]> getOutputType() {
            return CollectionUtils.getArrayType(type);
        }

        @Override
        public Block getBlock() {
            return Multiplexor.this;
        }

        @Override
        public String getName() {
            return "";
        }
    }

    public interface MultiplexorListener extends EventListener {

        void inputCountChanged(int count);
    }

    public class MultiplexorEditor extends AbstractBlockComponentEditor<Multiplexor<E>> {

        protected final JSpinner spinner = new JSpinner(new SpinnerNumberModel(inputCount, 1, 32, 1));

        public MultiplexorEditor(Window window) {
            super(window, Multiplexor.this);
            tabPane("Сommon").addLine("Input count", spinner);
            afterInit();
        }

        @Override
        protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
            block.setInputCount(((Number) spinner.getValue()).intValue());
        }
    }
}
