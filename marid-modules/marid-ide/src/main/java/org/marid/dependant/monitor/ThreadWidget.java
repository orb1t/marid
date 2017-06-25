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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Order(1)
public class ThreadWidget extends LineChart<Number, Number> {

    private final int count = 60;
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public ThreadWidget() {
        super(new NumberAxis(), new NumberAxis());
        getData().addAll(Arrays.asList(
                peekThreads(),
                daemonThreads(),
                threads()
        ));
        setAnimated(false);
    }

    @Autowired
    private void init(GridPane monitorGridPane) {
        monitorGridPane.add(this, 0, 1);
    }

    private Series<Number, Number> peekThreads() {
        final Series<Number, Number> series = new Series<>();
        series.setName(s("Peek threads"));
        series.getData().addAll(range(0, count).mapToObj(i -> new Data<Number, Number>(i, 0.0)).collect(toList()));
        return series;
    }

    private Series<Number, Number> daemonThreads() {
        final Series<Number, Number> series = new Series<>();
        series.setName(s("Daemon threads"));
        series.getData().addAll(range(0, count).mapToObj(i -> new Data<Number, Number>(i, 0.0)).collect(toList()));
        return series;
    }

    private Series<Number, Number> threads() {
        final Series<Number, Number> series = new Series<>();
        series.setName(s("Threads"));
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
        final Integer[] values = {
                threadMXBean.getPeakThreadCount(),
                threadMXBean.getDaemonThreadCount(),
                threadMXBean.getThreadCount()
        };
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
