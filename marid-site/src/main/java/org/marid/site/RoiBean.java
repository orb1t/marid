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
import com.tunyk.currencyconverter.api.Currency;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
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
    private Currency currency = Currency.USD;
    private float averagePrice = 50000.0f;
    private int systemsPerYear = 10;
    private float revenue = 0.1f;
    private int developmentTime = 12;
    private float costsPerMonth = 4000.0f;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public float getAveragePrice() {
        return averagePrice;
    }

    public int getSystemsPerYear() {
        return systemsPerYear;
    }

    public float getRevenue() {
        return revenue;
    }

    public int getDevelopmentTime() {
        return developmentTime;
    }

    public float getCostsPerMonth() {
        return costsPerMonth;
    }
    
    public float getRoi() {
        final float exp = developmentTime * costsPerMonth;
        final float rev = (developmentTime / 12.0f) * systemsPerYear * revenue * averagePrice;
        return rev / exp;
    }
    
    public Set<Currency> getCurrencies() {
        return EnumSet.allOf(Currency.class);
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }
    
    private mxGraph graph() {
        final String startText = localeBean.msg("Start");
        final String selectDaqText = localeBean.msg("Select a first DAQ system to do with Marid");
        final String protocolsText = localeBean.msg("Implement device protocols");
        final String webText = localeBean.msg("Run system as Web service");
        final String guiText = localeBean.msg("Make GUI to provide an ability to make such systems from scratch");
        final String docText = localeBean.msg("Document the API and make examples");
        final String buildSimilarText = localeBean.msg("Build similar system for customers and do support");
        final mxGraph graph = new mxGraph();
        final Object p = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            final Object start = graph.insertVertex(p, null, startText, 0, 0, 400, 40, "fillColor=#EEEE10");
            final Object select = graph.insertVertex(p, null, selectDaqText, 0, 0, 400, 40, "fillColor=#EEEE10");
            final Object protocols = graph.insertVertex(p, null, protocolsText, 0, 0, 400, 40, "fillColor=#FF4430");
            final Object web = graph.insertVertex(p, null, webText, 0, 0, 400, 40, "fillColor=#FF4030");
            final Object gui = graph.insertVertex(p, null, guiText, 0, 0, 400, 40, "fillColor=#FF4030");
            final Object doc = graph.insertVertex(p, null, docText, 0, 0, 300, 40, "fillColor=#FF4030");
            final Object buildSimilar = graph.insertVertex(p, null, buildSimilarText, 0, 0, 300, 40, "fillColor=#20FF55");
            graph.insertEdge(p, null, null, start, select);
            graph.insertEdge(p, null, null, select, protocols);
            graph.insertEdge(p, null, null, protocols, web);
            graph.insertEdge(p, null, null, web, gui);
            graph.insertEdge(p, null, null, gui, doc);
            graph.insertEdge(p, null, null, gui, buildSimilar);
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
