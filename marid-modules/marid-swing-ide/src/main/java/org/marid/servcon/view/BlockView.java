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

package org.marid.servcon.view;

import images.Images;
import org.marid.servcon.model.Block;
import org.marid.servcon.view.ga.LineSpecie;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.border.TitledBorder.CENTER;
import static javax.swing.border.TitledBorder.LEADING;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockView extends JPanel {

    public static final String MOVEABLE = "$moveable";

    protected final BlockEditor blockEditor;
    protected final Block block;
    protected final List<In> inputs = new ArrayList<>();
    protected final List<Out> outputs = new ArrayList<>();

    public BlockView(BlockEditor blockEditor, Block block) {
        this.blockEditor = blockEditor;
        this.block = block;
        setName(MOVEABLE);
        setOpaque(true);
        setBorder(BorderFactory.createRaisedSoftBevelBorder());
        final GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        g.setAutoCreateContainerGaps(true);
        final JToolBar toolBar = new JToolBar();
        toolBar.setName(MOVEABLE);
        toolBar.setFloatable(false);
        toolBar.setBorder(createEtchedBorder());
        toolBar.setBorderPainted(true);
        toolBar.add(titleLabel());
        toolBar.add(Box.createGlue());
        toolBar.addSeparator();
        toolBar.add(new MaridAction("Remove block", "removeWidget", e -> {
            blockEditor.remove(this);
        }));
        final GroupLayout.SequentialGroup vl = g.createSequentialGroup();
        final GroupLayout.SequentialGroup vr = g.createSequentialGroup();
        final GroupLayout.ParallelGroup hl = g.createParallelGroup();
        final GroupLayout.ParallelGroup hr = g.createParallelGroup();
        if (block.hasParameters()) {
            final JPanel paramPanel = panel("Parameters");
            for (final Block.Param param : block.getParameters()) {
                paramPanel.add(getIn(param));
            }
            vl.addComponent(paramPanel);
            hl.addComponent(paramPanel);
        }
        if (block.hasInputs()) {
            final JPanel inputsPanel = panel("Parameters");
            for (final Block.In in : block.getInputs()) {
                inputsPanel.add(getIn(in));
            }
            vl.addComponent(inputsPanel);
            hl.addComponent(inputsPanel);
        }
        final JPanel outputsPanel = panel("Outputs");
        outputsPanel.add(getOut(block.getSelfOutput()));
        for (final Block.Out out : block.getOutputs()) {
            outputsPanel.add(getOut(out));
        }
        vr.addComponent(outputsPanel);
        hr.addComponent(outputsPanel);
        g.setVerticalGroup(g.createSequentialGroup()
                .addComponent(toolBar)
                .addGroup(g.createParallelGroup()
                        .addGroup(vl)
                        .addGroup(vr)));
        g.setHorizontalGroup(g.createParallelGroup()
                .addComponent(toolBar)
                .addGroup(g.createSequentialGroup()
                        .addGroup(hl)
                        .addGap(10, 10, Integer.MAX_VALUE)
                        .addGroup(hr)));
        setLayout(g);
        validate();
    }

    private JLabel titleLabel() {
        final JLabel label = new JLabel(block.toString(), block.getVisualRepresentation(), SwingConstants.LEFT);
        label.setName(MOVEABLE);
        return label;
    }

    private JPanel panel(String title) {
        final JPanel panel = new JPanel(new GridLayout(0, 1)) {
            @Override
            public Dimension getMaximumSize() {
                final Dimension maximumSize = super.getMaximumSize();
                return new Dimension(maximumSize.width, getPreferredSize().height);
            }
        };
        panel.setBorder(createTitledBorder(createEtchedBorder(), s(title) + ":", LEADING, CENTER));
        return panel;
    }

    private In getIn(Block.In in) {
        final In inPort = new In(in);
        inputs.add(inPort);
        return inPort;
    }

    private Out getOut(Block.Out out) {
        final Out outPort = new Out(out);
        outputs.add(outPort);
        return outPort;
    }

    public class In extends JToggleButton implements ActionListener {

        public final Block.In in;

        protected In(Block.In in) {
            super(in.toString(), Images.getIcon(in.getMetaInfo().icon(), 24));
            setIconTextGap(20);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            this.in = in;
            addActionListener(this);
        }

        public Point connectionPoint() {
            synchronized (BlockView.this.getTreeLock()) {
                return new Point(
                        BlockView.this.getX() - 1,
                        BlockView.this.getY() + getParent().getY() + getY() + getHeight() / 2);
            }
        }

        public BlockEditor getEditor() {
            return blockEditor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (final Component component : blockEditor.getComponents()) {
                if (component instanceof BlockView) {
                    final BlockView bv = (BlockView) component;
                    bv.outputs.stream().filter(Out::isSelected).forEach(out -> {
                        out.setSelected(false);
                        blockEditor.blockLinks.add(new BlockLink<LineSpecie>(LineSpecie::new, LineSpecie[]::new, this, out));
                    });
                }
            }
            setSelected(false);
            blockEditor.repaint();
        }
    }

    public class Out extends JToggleButton implements ActionListener {

        public final Block.Out out;

        protected Out(Block.Out out) {
            super(out.toString(), Images.getIcon(out.getMetaInfo().icon(), 24));
            setIconTextGap(20);
            setHorizontalTextPosition(SwingConstants.LEFT);
            this.out = out;
        }

        public Point connectionPoint() {
            synchronized (BlockView.this.getTreeLock()) {
                return new Point(
                        BlockView.this.getX() + BlockView.this.getWidth() + 1,
                        BlockView.this.getY() + getParent().getY() + getY() + getHeight() / 2);
            }
        }

        public BlockEditor getEditor() {
            return blockEditor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
}
