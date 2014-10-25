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

package org.marid.bd.components;

import org.marid.bd.Block;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.WEST;
import static java.awt.SystemColor.control;
import static java.awt.SystemColor.controlLtHighlight;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.marid.bd.components.ArrowButton.ARROW_SIZE;

/**
 * @author Dmitry Ovchinnikov
 */
public class StandardBlockComponent<B extends Block> extends DefaultBlockComponent<B> {

    protected final Insets insets = new Insets(5, 0, 5, 0);
    protected final List<Input> inputs = new ArrayList<>();
    protected final List<Output> outputs = new ArrayList<>();

    public StandardBlockComponent(B block) {
        super(new BorderLayout(), block);
        setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
                final Graphics2D g = (Graphics2D) graphics.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setStroke(new BasicStroke(2.0f));
                    g.setColor(SystemColor.controlDkShadow);
                    g.drawRoundRect(ARROW_SIZE * 2, 0, c.getWidth() - ARROW_SIZE * 4, c.getHeight(), 5, 5);
                } finally {
                    g.dispose();
                }
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return insets;
            }

            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
        update();
    }

    public StandardBlockComponent(B block, Consumer<StandardBlockComponent<B>> consumer) {
        this(block);
        consumer.accept(this);
    }

    @Override
    public void paint(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        final Paint paint = g.getPaint();
        try {
            final LinearGradientPaint gradientPaint = new LinearGradientPaint(
                    0.0f, 0.0f, getWidth(), getHeight(),
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{controlLtHighlight, control, controlLtHighlight});
            g.setPaint(gradientPaint);
            g.fillRect(ARROW_SIZE * 2, 1, getWidth() - ARROW_SIZE * 4, getHeight() - 2);
        } finally {
            g.setPaint(paint);
        }
        super.paint(graphics);
    }

    @Override
    public void update() {
        final Component westComponent = ((BorderLayout) getLayout()).getLayoutComponent(WEST);
        final Component eastComponent = ((BorderLayout) getLayout()).getLayoutComponent(EAST);
        if (westComponent != null) {
            remove(westComponent);
        }
        if (eastComponent != null) {
            remove(eastComponent);
        }
        add(inputsPanel(), WEST);
        add(outputsPanel(), EAST);
    }

    @Override
    public List<Input> getInputs() {
        return inputs;
    }

    @Override
    public List<Output> getOutputs() {
        return outputs;
    }

    public JPanel inputsPanel() {
        inputs.clear();
        final JPanel panel = new JPanel();
        panel.setOpaque(false);
        if (block.getInputs().isEmpty()) {
            panel.add(Box.createHorizontalStrut(2 * ARROW_SIZE));
            return panel;
        }
        final GroupLayout g = new GroupLayout(panel);
        g.setAutoCreateGaps(true);
        final GroupLayout.SequentialGroup v = g.createSequentialGroup();
        final GroupLayout.SequentialGroup h = g.createSequentialGroup();
        final GroupLayout.ParallelGroup hi = g.createParallelGroup(LEADING);
        final GroupLayout.ParallelGroup hl = g.createParallelGroup(LEADING);
        h.addGroup(hi).addGroup(hl);
        h.addGap(ARROW_SIZE);
        final Map<DefaultInput, JLabel> map = new LinkedHashMap<>();
        for (final Block.Input in : block.getInputs()) {
            final DefaultInput input = new DefaultInput(in);
            inputs.add(input);
            map.put(input, input.getAssociatedLabel());
        }
        v.addGap(0, 0, Integer.MAX_VALUE);
        map.forEach((i, l) -> {
            v.addGroup(g.createParallelGroup(BASELINE).addComponent(i).addComponent(l));
            hi.addComponent(i);
            hl.addComponent(l);
        });
        v.addGap(0, 0, Integer.MAX_VALUE);
        g.setVerticalGroup(v);
        g.setHorizontalGroup(h);
        panel.setLayout(g);
        return panel;
    }

    public JPanel outputsPanel() {
        outputs.clear();
        final JPanel panel = new JPanel();
        panel.setOpaque(false);
        if (block.getOutputs().isEmpty()) {
            return panel;
        }
        final GroupLayout g = new GroupLayout(panel);
        g.setAutoCreateGaps(true);
        final GroupLayout.SequentialGroup v = g.createSequentialGroup();
        final GroupLayout.SequentialGroup h = g.createSequentialGroup();
        final GroupLayout.ParallelGroup hl = g.createParallelGroup(LEADING);
        final GroupLayout.ParallelGroup ho = g.createParallelGroup(LEADING);
        h.addGap(ARROW_SIZE);
        h.addGroup(hl).addGroup(ho);
        final Map<JLabel, DefaultOutput> map = new LinkedHashMap<>();
        for (final Block.Output out : block.getOutputs()) {
            final DefaultOutput output = new DefaultOutput(out);
            outputs.add(output);
            map.put(output.getAssociatedLabel(), output);
        }
        v.addGap(0, 0, Integer.MAX_VALUE);
        map.forEach((l, o) -> {
            v.addGroup(g.createParallelGroup(BASELINE).addComponent(l).addComponent(o));
            hl.addComponent(l);
            ho.addComponent(o);
        });
        v.addGap(0, 0, Integer.MAX_VALUE);
        g.setVerticalGroup(v);
        g.setHorizontalGroup(h);
        panel.setLayout(g);
        return panel;
    }
}
