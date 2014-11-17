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

package org.marid.ide.frames.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marid.dyn.MetaInfo;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.frames.CloseableFrame;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.profile.Profile;
import org.marid.jmx.IdeJmxAttribute;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Dmitry Ovchinnikov.
 */
@CloseableFrame
@MetaInfo(name = "Graph")
public class GraphFrame extends MaridFrame implements DndTarget<IdeJmxAttribute> {

    private final Profile profile;
    private final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private final Timer timer;

    @Autowired
    public GraphFrame(GenericApplicationContext context) {
        super(context, LS.s("Graph"));
        profile = context.getBean(ProfileManager.class).getCurrentProfile();
        chart = ChartFactory.createTimeSeriesChart(s("Chart"), s("Time"), s("Value"), timeSeriesCollection);
        timer = new Timer(getPref("delay", 1_000), ev -> {
            for (int i = 0; i < timeSeriesCollection.getSeriesCount(); i++) {
                timeSeriesCollection.getSeries(i).add(new Second(new Date()), ThreadLocalRandom.current().nextDouble());
            }
        });
        setTransferHandler(new MaridTransferHandler());
        centerPanel.add(chartPanel = new ChartPanel(chart));
    }

    @Override
    public int getTargetDndActionsSupported() {
        return DND_LINK;
    }

    @Override
    protected void fillActions() {
        addAction("/Control//Start", "Start", "start", this::start).enableToolbar();
        addAction("/Control//Stop", "Stop", "stop", this::stop).setEnabledState(false).enableToolbar();
    }

    public void start(ActionEvent event) {
        timer.start();
        actionByKey("/Control//Start").setEnabled(false);
        actionByKey("/Control//Stop").setEnabled(true);
    }

    public void stop(ActionEvent event) {
        timer.stop();
        actionByKey("/Control//Start").setEnabled(true);
        actionByKey("/Control//Stop").setEnabled(false);
    }

    @Override
    public boolean dropDndObject(IdeJmxAttribute object, TransferHandler.TransferSupport support) {
        timeSeriesCollection.addSeries(new TimeSeries(object));
        return true;
    }
}
