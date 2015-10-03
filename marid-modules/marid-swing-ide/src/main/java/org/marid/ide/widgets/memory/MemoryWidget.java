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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marid.dyn.MetaInfo;
import org.marid.ide.widgets.ConfigurableWidget;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.forms.ConfData;
import org.marid.swing.actions.ShowHideListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.util.Date;

import static org.jfree.chart.ChartFactory.createTimeSeriesChart;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo(name = "Memory consumption")
@PrototypeComponent
public class MemoryWidget extends ConfigurableWidget implements ShowHideListener {

    private final MemoryWidgetConfiguration configuration;
    private final Runtime runtime = Runtime.getRuntime();
    private final TimeSeries totalMemorySeries;
    private final TimeSeries freeMemorySeries;
    private final Timer timer;

    @Autowired
    public MemoryWidget(MemoryWidgetConfiguration configuration) {
        super("Memory");
        this.configuration = configuration;
        totalMemorySeries = createTimeSeries("Total", configuration.historySize.get());
        freeMemorySeries = createTimeSeries("Free", configuration.historySize.get());
        timer = new Timer(configuration.updateInterval.get() * 1000, e -> {
            final double totalMemory = runtime.totalMemory() / 1e6;
            final double freeMemory = runtime.freeMemory() / 1e6;
            final Second second = new Second(new Date());
            totalMemorySeries.add(second, totalMemory);
            freeMemorySeries.add(second, freeMemory);
        });
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(totalMemorySeries);
        dataset.addSeries(freeMemorySeries);
        final JFreeChart chart = createTimeSeriesChart(s("Memory"), s("Time"), s("Memory") + ", MiB", dataset);
        add(new ChartPanel(chart,
                configuration.useBuffer.get(),
                configuration.save.get(),
                configuration.print.get(),
                configuration.zoom.get(),
                configuration.tooltips.get()));
        configuration.updateInterval.addListener(this, n -> timer.setDelay(n * 1000));
        addComponentListener(this);
    }

    private TimeSeries createTimeSeries(String title, int historySize) {
        final TimeSeries series = new TimeSeries(s(title));
        series.setMaximumItemCount(historySize * 60);
        RegularTimePeriod second = new Second(new Date()).previous();
        for (int i = 0; i < series.getMaximumItemCount(); i++) {
            second = second.previous();
        }
        for (int i = 0; i < series.getMaximumItemCount(); i++, second = second.next()) {
            series.add(second, 0.0);
        }
        return series;
    }

    @Override
    public void componentShown(ComponentEvent e) {
        timer.start();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        timer.stop();
    }

    @Override
    public ConfData configuration() {
        return configuration;
    }
}