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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class IdeIndicators extends VBox {

    private final List<Callable<Runnable>> updateTasks = new ArrayList<>();

    @PostConstruct
    private void initCpuLoad() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName osObjectName = new ObjectName("java.lang", "type", "OperatingSystem");
        final MBeanInfo beanInfo = server.getMBeanInfo(osObjectName);
        final MBeanAttributeInfo processCpuLoadAttribute = Stream.of(beanInfo.getAttributes())
                .filter(a -> "ProcessCpuLoad".equals(a.getName()))
                .findFirst()
                .orElse(null);
        if (processCpuLoadAttribute != null) {
            final ProgressIndicator cpuIndicator = new ProgressIndicator();
            updateTasks.add(() -> {
                final Number value = (Number) server.getAttribute(osObjectName, "ProcessCpuLoad");
                return () -> cpuIndicator.setProgress(value.doubleValue());
            });
            getChildren().add(cpuIndicator);
        }
    }

    @Scheduled(fixedDelay = 1_000L)
    private void update() throws Exception {
        for (final Callable<Runnable> task : updateTasks) {
            Platform.runLater(task.call());
        }
    }
}
