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

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.canvas.mxImageCanvas;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.imageio.ImageIO;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class SysStructBean {
    
    private int controllerCount = 10;
    private int meterCount = 10;
    private static final Map<Integer, byte[]> cache = new WeakHashMap<Integer, byte[]>();

    public int getMeterCount() {
        return meterCount;
    }

    public int getControllerCount() {
        return controllerCount;
    }

    public void setMeterCount(int meterCount) {
        this.meterCount = meterCount;
    }

    public void setControllerCount(int controllerCount) {
        this.controllerCount = controllerCount;
    }
    
    private mxGraph graph() {
        final mxGraph graph = new mxGraph();
        final Object p = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            final Object server = graph.insertVertex(p, null, "Server", 0, 0, 200, 60, "fillColor=#EA5D01");
            for (int c = 0; c < controllerCount; c++) {
                final Object controller = graph.insertVertex(p, null, "C" + (c + 1), 0, 0, 25, 20, "fillColor=yellow");
                final Object cEdge = graph.insertEdge(p, null, "", server, controller);
                for (int m = 0; m < meterCount; m++) {
                    final Object meter = graph.insertVertex(p, null, "M" + (m + 1), 0, 0, 25, 20);
                    final Object mEdge = graph.insertEdge(p, null, "", controller, meter);
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
        return graph;
    }
            
    public StreamedContent structImage(int size) throws IOException {
        final int key = size * 1024 * 1024 + controllerCount * 1024 + meterCount;
        synchronized (cache) { 
            final byte[] data = cache.get(key);
            if (data != null) {
                return new DefaultStreamedContent(new ByteArrayInputStream(data), "image/png");
            }
        }
        final mxGraph graph = graph();
        final mxFastOrganicLayout layout = new mxFastOrganicLayout(graph);
        layout.setInitialTemp(100.0);
        layout.execute(graph.getDefaultParent());
        final mxGraphics2DCanvas g2dc = new mxGraphics2DCanvas();
        final mxRectangle bounds = graph.getView().getGraphBounds();
        final int w = (int) bounds.getWidth() + 1;
        final int h = (int) bounds.getHeight() + 1;
        final mxImageCanvas canvas = new mxImageCanvas(g2dc, w, h, Color.WHITE, true);
        graph.drawGraph(canvas);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(canvas.getImage(), "PNG", bos);
        final byte[] data = bos.toByteArray();
        synchronized (cache) {
            cache.put(key, data);
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(data), "image/png");
    }
}
