/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.dependant.monitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Order(1)
public class ClassLoadingWidget extends LineChart<Number, Number> {

  private final int count = 60;
  private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

  public ClassLoadingWidget() {
    super(new NumberAxis(), new NumberAxis());
    getData().addAll(Arrays.asList(
        loadedClasses(),
        unloadedClasses()
    ));
    setAnimated(false);
  }

  @Autowired
  private void init(GridPane monitorGridPane) {
    monitorGridPane.add(this, 1, 1);
  }

  private Series<Number, Number> loadedClasses() {
    final Series<Number, Number> series = new Series<>();
    series.nameProperty().bind(ls("Loaded classes"));
    series.getData().addAll(range(0, count).mapToObj(i -> new Data<Number, Number>(i, 0.0)).collect(toList()));
    return series;
  }

  private Series<Number, Number> unloadedClasses() {
    final Series<Number, Number> series = new Series<>();
    series.nameProperty().bind(ls("Unloaded classes"));
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
    final Long[] values = {classLoadingMXBean.getTotalLoadedClassCount(), classLoadingMXBean.getUnloadedClassCount()};
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
