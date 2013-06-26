/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.swing.impl.windows;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marid.ide.itf.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import static images.Images.getImage;
import static java.util.Arrays.asList;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProcessWindowImpl extends JFrame implements Window, ActionListener {

    private static final long serialVersionUID = -2018890141352904632L;
    private final Timer timer = new Timer(30, this);
    private final TimeSeries process = new TimeSeries("process1");
    private final TimeSeriesCollection dataset = new TimeSeriesCollection(process);
    private final JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Process", "Time", "Value", dataset, false, false, false);
    private final ChartPanel chartPanel = new ChartPanel(chart, true);
    private double t = 0.0;

    public ProcessWindowImpl() {
        super("Graph");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(asList(getImage("process16.png"), getImage("process20.png")));
        add(chartPanel);
        setPreferredSize(new Dimension(600, 600));
        pack();
        setLocationByPlatform(true);
        process.setMaximumItemCount(240);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                timer.start();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        t += 0.1;
        process.add(new Millisecond(new Date()), 5.0 * Math.sin(t) + Math.random() - 0.5);
    }
}
