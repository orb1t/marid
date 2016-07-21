/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.hmi.db;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;
import javafx.util.converter.FormatStringConverter;
import org.marid.db.dao.NumericReader;
import org.marid.db.data.DataRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DateFormat;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SimpleGraphWidget extends Stage {

    private final ScheduledExecutorService timer;
    private final NumericReader reader;
    private final long tag;
    private final String name;
    private final long period;
    private final ObservableList<LineChart.Data<Number, Number>> data;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final LineChart.Series<Number, Number> series;
    private final LineChart<Number, Number> chart;
    private final AtomicLong lastTime;

    private ScheduledFuture<?> timerTask;

    public SimpleGraphWidget(ScheduledExecutorService timer, NumericReader reader, SimpleGraphWidgetProperties conf) {
        this.timer = timer;
        this.reader = reader;
        this.tag = conf.getTag();
        this.name = conf.getName();
        this.period = conf.getPeriod();
        this.data = FXCollections.observableArrayList();
        this.xAxis = new NumberAxis();
        this.xAxis.setForceZeroInRange(false);
        this.xAxis.setTickLabelFormatter(new FormatStringConverter<>(DateFormat.getTimeInstance(DateFormat.MEDIUM)));
        this.yAxis = new NumberAxis();
        this.series = new LineChart.Series<>(name, data);
        this.chart = new LineChart<>(xAxis, yAxis);
        this.lastTime = new AtomicLong(System.currentTimeMillis() - period * conf.getTickCount());
        this.chart.getData().add(series);
        setResizable(true);
        setTitle(s("Graph %s (%d), period = %d ms", name, tag, period));
        setScene(new Scene(chart));
    }

    private void updateData() {
        final Instant last = Instant.ofEpochMilli(lastTime.getAndSet(System.currentTimeMillis()));
        final Instant now = Instant.ofEpochMilli(lastTime.get());
        final long minTime = now.toEpochMilli() - period * data.size();
        final List<DataRecord<Double>> records = reader.fetchRecords(new long[] {tag}, last, now);
        Platform.runLater(() -> {
            data.removeIf(d -> d.getXValue().longValue() < minTime);
            records.forEach(r -> data.add(new LineChart.Data<>(r.getTimestamp().toEpochMilli(), r.getValue())));
        });
    }

    @PostConstruct
    private void init() {
        timerTask = timer.scheduleWithFixedDelay(this::updateData, period, period, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    private void destroy() {
        if (timerTask != null) {
            timerTask.cancel(false);
        }
    }
}
