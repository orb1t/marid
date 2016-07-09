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

package org.marid.dependant.monitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.GridPane;
import org.marid.spring.annotation.TypeQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@TypeQualifier(MonitorConfiguration.class)
@Order(1)
public class OperatingSystemWidget extends LineChart<Number, Number> {

    private final int count = 60;
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    public OperatingSystemWidget() {
        super(new NumberAxis(), new NumberAxis());
        getData().addAll(Arrays.asList(
                systemLoad()
        ));
        setAnimated(false);
    }

    @Autowired
    private void init(GridPane monitorGridPane) {
        monitorGridPane.add(this, 1, 0);
    }

    private Series<Number, Number> systemLoad() {
        final Series<Number, Number> series = new Series<>();
        series.setName(s("System load average"));
        series.getData().addAll(range(0, count).mapToObj(i -> new Data<Number, Number>(i, 0.0)).collect(toList()));
        return series;
    }

    @Override
    public NumberAxis getXAxis() {
        return (NumberAxis) super.getXAxis();
    }

    @Override
    public NumberAxis getYAxis() {
        return (NumberAxis) super.getYAxis();
    }

    @Scheduled(fixedRate = 250L)
    private void tick() {
        final double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        final Double[] values = {systemLoadAverage};
        Platform.runLater(() -> {
            for (int i = 0; i < getData().size(); i++) {
                final Series<Number, Number> series = getData().get(i);
                final ObservableList<Data<Number, Number>> data = series.getData();
                for (int j = 1; j < count; j++) {
                    data.get(j - 1).setYValue(data.get(j).getYValue());
                }
                data.get(count - 1).setYValue(values[i]);
            }
        });
    }
}
