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
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class SysStructBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(SysStructBean.class.getName());
    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;
    @ManagedProperty("#{currencyConverterBean}")
    private CurrencyConverterBean currencyConverterBean;
    private int controllerCount = 5;
    private int meterCount = 5;
    private String currency = "USD";
    private MeterLinkType meterLinkType = MeterLinkType.LAN;
    private ControllerLinkType controllerLinkType = ControllerLinkType.LAN;

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public void setCurrencyConverterBean(CurrencyConverterBean currencyConverterBean) {
        this.currencyConverterBean = currencyConverterBean;
    }

    public Set<MeterLinkType> getMeterLinkTypes() {
        return EnumSet.allOf(MeterLinkType.class);
    }

    public Set<ControllerLinkType> getControllerLinkTypes() {
        return EnumSet.allOf(ControllerLinkType.class);
    }

    public int getMeterCount() {
        return meterCount;
    }

    public int getControllerCount() {
        return controllerCount;
    }

    public MeterLinkType getMeterLinkType() {
        return meterLinkType;
    }

    public ControllerLinkType getControllerLinkType() {
        return controllerLinkType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setMeterCount(int meterCount) {
        this.meterCount = meterCount;
    }

    public void setControllerCount(int controllerCount) {
        this.controllerCount = controllerCount;
    }

    public void setMeterLinkType(MeterLinkType meterLinkType) {
        this.meterLinkType = meterLinkType;
    }

    public void setControllerLinkType(ControllerLinkType controllerLinkType) {
        this.controllerLinkType = controllerLinkType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private mxGraph graph() {
        final mxGraph graph = new mxGraph();
        final Object p = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            final Object server = graph.insertVertex(p, null, "Server", 0, 0, 200, 60, "fillColor=#EA5D01;fontSize=24");
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
    
    private File cachedFile() {
        return new File(System.getProperty("java.io.tmpdir"), controllerCount + "_" + meterCount  + ".png");
    }
    
    private DefaultStreamedContent getCached() {
        final File cachedFile = cachedFile();
        synchronized (SysStructBean.class) {
            if (cachedFile.exists()) {
                try {
                    return new DefaultStreamedContent(new FileInputStream(cachedFile), "image/png");
                } catch (Exception x) {
                    LOG.log(Level.WARNING, "Unable to get cached image", x);
                }
            }
        }
        return null;
    }
    
    private boolean writeCached(RenderedImage image) {
        final File cachedFile = cachedFile();
        synchronized (SysStructBean.class) {
            if (!cachedFile.exists()) {
                try {
                    ImageIO.write(image, "PNG", cachedFile);
                    cachedFile.deleteOnExit();
                    return true;
                } catch (Exception x) {
                    LOG.log(Level.WARNING, "Unable to write to the cache", x);
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public StreamedContent structImage() throws IOException {
        DefaultStreamedContent c = getCached();
        if (c != null) {
            return c;
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
        final boolean cached = writeCached(canvas.getImage());
        if (cached) {
            c = getCached();
            if (c != null) {
                return c;
            }
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(canvas.getImage(), "PNG", bos);
        final byte[] data = bos.toByteArray();
        return new DefaultStreamedContent(new ByteArrayInputStream(data), "image/png");
    }

    private float getPriceInUsd() {
        final float controllerSelfPrice = 200.0f;
        final float controllerCasePrice = 50.0f;
        final float controllerTransmitterPrice;
        switch (controllerLinkType) {
            case LAN:
                controllerTransmitterPrice = 0.0f;
                break;
            case MOBILE:
                controllerTransmitterPrice = 100.0f;
                break;
            case WIFI:
                controllerTransmitterPrice = 30.0f;
                break;
            default:
                controllerTransmitterPrice = 0.0f;
                break;
        }
        final float controllerReceiverPrice;
        switch (meterLinkType) {
            case LAN:
                controllerReceiverPrice = 0.0f;
                break;
            case RS485_CAN:
                controllerReceiverPrice = (1 + meterCount / 16.0f) * 30.0f;
                break;
            case WIFI:
                controllerReceiverPrice = 30.0f;
                break;
            case ZIGBEE:
                controllerReceiverPrice = 100.0f;
                break;
            default:
                controllerReceiverPrice = 0.0f;
                break;
        }
        final float serverPrice = (1 + (meterCount * controllerCount) / 100.0f) * 300.0f;
        final float controllerPrice = controllerSelfPrice
                + controllerReceiverPrice
                + controllerTransmitterPrice
                + controllerCasePrice;
        return serverPrice + controllerPrice * controllerCount;
    }

    public float getPrice() {
        return currencyConverterBean.convertTo(getPriceInUsd(), currency);
    }

    public String getPriceText() {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(localeBean.getLocale());
        return numberFormat.format(getPrice());
    }
}
