/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.site;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class GraphVisualizer implements Runnable {

    private final JFrame frame = new JFrame("Graph Visualizer");
    private final mxGraph graph = new mxGraph();

    public GraphVisualizer() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        fillGraph();
        frame.add(new mxGraphComponent(graph));
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void fillGraph() {
        final Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80, 30);
            Object v2 = graph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
            graph.insertEdge(parent, null, "Edge", v1, v2);
        } finally {
            graph.getModel().endUpdate();
        }
    }

    public static void main(String... args) throws Exception {
        EventQueue.invokeLater(new GraphVisualizer());
    }

    @Override
    public void run() {
        frame.setVisible(true);
    }
}
