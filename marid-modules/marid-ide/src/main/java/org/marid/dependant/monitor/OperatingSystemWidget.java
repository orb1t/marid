package org.marid.dependant.monitor;

import com.sun.management.OperatingSystemMXBean;
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
import java.util.function.DoubleSupplier;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Order(1)
public class OperatingSystemWidget extends LineChart<Number, Number> {

    private final int count = 60;
    private final DoubleSupplier loadSupplier;

    public OperatingSystemWidget() {
        super(new NumberAxis(), new NumberAxis());
        this.loadSupplier = loadSupplier();
        getData().add(systemLoad());
        setAnimated(false);
    }

    @Autowired
    private void init(GridPane monitorGridPane) {
        monitorGridPane.add(this, 1, 0);
    }

    private DoubleSupplier loadSupplier() {
        try {
            Class.forName("com.sun.management.OperatingSystemMXBean");
            return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())::getProcessCpuLoad;
        } catch (Exception x) {
            return ManagementFactory.getOperatingSystemMXBean()::getSystemLoadAverage;
        }
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
        final Double[] values = {loadSupplier.getAsDouble()};
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
