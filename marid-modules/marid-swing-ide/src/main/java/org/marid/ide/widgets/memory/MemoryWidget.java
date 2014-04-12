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
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.marid.dyn.MetaInfo;
import org.marid.ide.IdeFrame;
import org.marid.ide.widgets.Widget;
import org.marid.pref.PrefSupport;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@MetaInfo(name = "Memory consumption")
public class MemoryWidget extends Widget implements PrefSupport, MemoryWidgetConfiguration {

    private final XYDataset dataset;
    private final JFreeChart chart;

    public MemoryWidget(IdeFrame owner) {
        super(owner, "Memory");
        dataset = new TimeSeriesCollection();
        chart = ChartFactory.createTimeSeriesChart(s("Memory"), s("Time"), s("Memory") + ", MiB", dataset);
        add(new ChartPanel(chart, useBuffer.get(), save.get(), print.get(), zoom.get(), tooltips.get()));
        pack();
    }
}

