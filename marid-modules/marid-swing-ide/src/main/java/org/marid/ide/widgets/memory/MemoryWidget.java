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

package org.marid.ide.widgets.memory;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marid.dyn.MetaInfo;
import org.marid.ide.IdeFrame;
import org.marid.ide.widgets.Widget;
import org.marid.pref.PrefSupport;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.util.Date;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo(name = "Memory consumption")
public class MemoryWidget extends Widget implements PrefSupport, MemoryWidgetConfiguration {

    private final TimeSeriesCollection dataset;
    private final JFreeChart chart;
    private final Runtime runtime = Runtime.getRuntime();
    private final TimeSeries totalMemorySeries = createTimeSeries("Total");
    private final TimeSeries freeMemorySeries = createTimeSeries("Free");
    private final Timer timer = new Timer(updateInterval.get() * 1000, e -> {
        final double totalMemory = runtime.totalMemory() / 1e6;
        final double freeMemory = runtime.freeMemory() / 1e6;
        final Second second = new Second(new Date());
        totalMemorySeries.add(second, totalMemory);
        freeMemorySeries.add(second, freeMemory);
    });

    public MemoryWidget(IdeFrame owner) {
        super(owner, "Memory");
        dataset = new TimeSeriesCollection();
        dataset.addSeries(totalMemorySeries);
        dataset.addSeries(freeMemorySeries);
        chart = ChartFactory.createTimeSeriesChart(s("Memory"), s("Time"), s("Memory") + ", MiB", dataset);
        add(new ChartPanel(chart, useBuffer.get(), save.get(), print.get(), zoom.get(), tooltips.get()));
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                timer.start();
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                timer.stop();
            }
        });
        updateInterval.addConsumer(this, (o, n) -> timer.setDelay(n * 1000));
        pack();
    }

    private TimeSeries createTimeSeries(String title) {
        final TimeSeries series = new TimeSeries(s(title));
        series.setMaximumItemCount(historySize.get() * 60);
        RegularTimePeriod second = new Second(new Date()).previous();
        for (int i = 0; i < series.getMaximumItemCount(); i++) {
            second = second.previous();
        }
        for (int i = 0; i < series.getMaximumItemCount(); i++, second = second.next()) {
            series.add(second, 0.0);
        }
        return series;
    }
}

