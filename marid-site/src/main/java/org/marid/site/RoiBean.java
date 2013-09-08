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
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.imageio.ImageIO;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class RoiBean implements Serializable {
    
    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }
    
    private mxGraph graph() {
        final String startText = localeBean.msg("Start");
        final String selectDaqText = localeBean.msg("Select a first DAQ system to do with Marid");
        final mxGraph graph = new mxGraph();
        final Object p = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            final Object start = graph.insertVertex(p, null, startText, 0, 0, 400, 40, "fillColor=#23FF71");
            final Object select = graph.insertVertex(p, null, selectDaqText, 0, 0, 400, 40, "fillColor=#13BB79");
            graph.insertEdge(p, null, null, start, select);
        } finally {
            graph.getModel().endUpdate();
        }
        return graph;
    }
    
    public StreamedContent getImage() throws Exception {
        final mxGraph graph = graph();
        final mxGraphLayout layout = new mxHierarchicalLayout(graph);
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
        return new DefaultStreamedContent(new ByteArrayInputStream(data), "image/png");
    }
}
