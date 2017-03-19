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

package org.marid.ide.status;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.marid.jfx.icons.FontIcon.D_TELEVISION;
import static org.marid.jfx.icons.FontIcon.O_CLOCK;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Service
@Lazy(false)
public class IdeIndicators {

    private final List<Callable<Runnable>> updateTasks = new ArrayList<>();
    private final IdeStatusBar statusBar;

    @Autowired
    public IdeIndicators(IdeStatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @OrderedInit(1)
    public void initCpuLoad() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName osObjectName = new ObjectName("java.lang", "type", "OperatingSystem");
        final MBeanInfo beanInfo = server.getMBeanInfo(osObjectName);
        final MBeanAttributeInfo processCpuLoadAttribute = Stream.of(beanInfo.getAttributes())
                .filter(a -> "ProcessCpuLoad".equals(a.getName()))
                .findFirst()
                .orElse(null);
        if (processCpuLoadAttribute != null) {
            final ProgressBar indicator = new ProgressBar(0);
            updateTasks.add(() -> {
                final Number value = (Number) server.getAttribute(osObjectName, "ProcessCpuLoad");
                return () -> indicator.setProgress(value.doubleValue());
            });
            add(glyphIcon(D_TELEVISION, 16), indicator);
        }
    }

    @OrderedInit(2)
    public void initDateTime() throws Exception {
        final Label timeLabel = new Label("", glyphIcon(O_CLOCK, 16));
        final DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral(' ')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter();
        updateTasks.add(() -> {
            final ZonedDateTime now = Instant.now().atZone(ZoneId.systemDefault());
            final String time = now.format(timeFormatter);
            return () -> timeLabel.setText(time);
        });
        timeLabel.setMaxHeight(Double.MAX_VALUE);
        add(timeLabel);
    }

    private void add(Node... nodes) {
        if (!statusBar.getChildren().isEmpty()) {
            statusBar.getChildren().add(new Separator(Orientation.VERTICAL));
        }
        statusBar.getChildren().addAll(nodes);
    }

    @Scheduled(fixedDelay = 1_000L)
    private void update() throws Exception {
        for (final Callable<Runnable> task : updateTasks) {
            Platform.runLater(task.call());
        }
    }
}
