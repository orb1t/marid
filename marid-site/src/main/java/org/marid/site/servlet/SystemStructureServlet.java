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
package org.marid.site.servlet;

import com.mxgraph.canvas.mxSvgCanvas;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.layout.mxPartitionLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.layout.orthogonal.mxOrthogonalLayout;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author Dmitry Ovchinnikov
 */
public class SystemStructureServlet extends HttpServlet {
    
    private mxGraphLayout layout(HttpServletRequest r, mxGraph graph) {
        final String l = r.getParameter("l");
        final mxGraphLayout layout;
        if ("c".equals(l)) {
            layout = new mxCircleLayout(graph);
        } else if ("o".equals(l)) {
            layout = new mxOrganicLayout(graph);
        } else if ("ct".equals(l)) {
            layout = new mxCompactTreeLayout(graph);
        } else if ("h".equals(l)) {
            layout = new mxHierarchicalLayout(graph);
        } else if ("ot".equals(l)) {
            layout = new mxOrthogonalLayout(graph);
        } else if ("p".equals(l)) {
            layout = new mxParallelEdgeLayout(graph);
        } else if ("pt".equals(l)) {
            layout = new mxPartitionLayout(graph);
        } else if ("s".equals(l)) {
            layout = new mxStackLayout(graph);
        } else {
            layout = new mxFastOrganicLayout(graph);
        }
        try {
            final BeanInfo bi = Introspector.getBeanInfo(layout.getClass());
            for (final PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                final Method method = pd.getWriteMethod();
                if (method == null) {
                    continue;
                }
                if (method.getParameterTypes().length != 1) {
                    continue;
                }
                final String name = pd.getName();
                final String value = r.getParameter(name);
                if (value == null) {
                    continue;
                }
                final Class<?> c = method.getParameterTypes()[0];
                if (c == double.class) {
                    method.invoke(layout, Double.parseDouble(value));
                } else if (c == int.class) {
                    method.invoke(layout, Integer.parseInt(value));
                } else if (c == float.class) {
                    method.invoke(layout, Float.parseFloat(value));
                } else if (c == long.class) {
                    method.invoke(layout, Long.parseLong(value));
                } else if (c == boolean.class) {
                    method.invoke(layout, Boolean.parseBoolean(value));
                }
            }
        } catch (IntrospectionException x) {
            throw new IllegalStateException(x);
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
        return layout;
    }
    
    private mxGraph graph(SystemConfig sc) {
        final mxGraph graph = new mxGraph();
        final Object p = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            final Object server = graph.insertVertex(p, null, "Server", 0, 0, 200, 60, "fillColor=#EA5D01");
            for (int c = 0; c < sc.controllerCount; c++) {
                final Object controller = graph.insertVertex(p, null, "C" + (c + 1), 0, 0, 25, 20, "fillColor=yellow");
                final Object cEdge = graph.insertEdge(p, null, "", server, controller);
                for (int m = 0; m < sc.meterCount; m++) {
                    final Object meter = graph.insertVertex(p, null, "M" + (m + 1), 0, 0, 25, 20);
                    final Object mEdge = graph.insertEdge(p, null, "", controller, meter);
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }
        return graph;
    }
    
    private String viewBox(HttpServletRequest req, mxRectangle bounds) {
        final String w = req.getParameter("w");
        final String h = req.getParameter("h");
        final int width, height;
        if (w != null) {
            width = Integer.parseInt(w);
            if (h != null) {
                height = Integer.parseInt(h);
            } else {
                height = (int) ((bounds.getHeight() / bounds.getWidth()) * width);
            }
        } else {
            if (h != null) {
                height = Integer.parseInt(h);
                width = (int) ((bounds.getWidth() / bounds.getHeight()) * height);
            } else {
                width = (int) bounds.getWidth() + 1;
                height = (int) bounds.getHeight() + 1;
            }
        }
        return "0 0 " + width + " " + height;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("image/svg+xml");
        final mxGraph graph = graph(new SystemConfig(req));
        final mxGraphLayout layout = layout(req, graph);
        layout.execute(graph.getDefaultParent());
        final mxRectangle bounds = graph.getView().getGraphBounds();
        final Document document;
        final Element svg;
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.newDocument();
            svg = (Element) document.appendChild(document.createElement("svg"));
            svg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
            svg.setAttribute("version", "1.1");
        } catch (ParserConfigurationException x) {
            throw new IOException(x);
        }
        final mxSvgCanvas canvas = new mxSvgCanvas(document);
        graph.drawGraph(canvas);
        if ("auto".equals(req.getParameter("scale"))) {
            svg.setAttribute("width", "100%");
            svg.setAttribute("height", "100%");
        } else {
            svg.setAttribute("width", Integer.toString((int) bounds.getWidth() + 1));
            svg.setAttribute("height", Integer.toString((int) bounds.getHeight() + 1));
        }
        svg.setAttribute("viewBox", viewBox(req, bounds));
        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            t.transform(new DOMSource(document), new StreamResult(resp.getOutputStream()));
        } catch (TransformerConfigurationException x) {
            throw new IOException(x);
        } catch (TransformerException x) {
            throw new IOException(x);
        }
    }
    
    private class SystemConfig {
        
        private final int controllerCount;
        private final int meterCount;
        
        public SystemConfig(HttpServletRequest r) {
            final String cc = r.getParameter("cc");
            final String mc = r.getParameter("mc");
            controllerCount = cc == null ? 10 : Integer.parseInt(cc);
            meterCount = mc == null ? 10 : Integer.parseInt(mc);
        }
    }
}
