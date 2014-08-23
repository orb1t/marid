/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.site;

import java.io.Serializable;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Time;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.marid.site.time.ChartTime;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class JmxBean extends TimerTask implements Serializable {

    private Timer timer;
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private final ClassLoadingMXBean clb = ManagementFactory.getClassLoadingMXBean();
    private final Runtime runtime = Runtime.getRuntime();
    private final SortedMap<Object, Number> usedMemMap = new ConcurrentSkipListMap<>();
    private final SortedMap<Object, Number> freeMemMap = new ConcurrentSkipListMap<>();
    private final SortedMap<Object, Number> cpuMap = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        timer = new Timer();
        timer.schedule(this, 0L, 1000L);
    }

    @PreDestroy
    public void destroy() {
        timer.cancel();
        timer = null;
    }

    private String formatNumber(Number number) {
        final Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        final NumberFormat format = NumberFormat.getNumberInstance(locale);
        return format.format(number);
    }

    public String getCommitedMemory() {
        return formatNumber(memory.getHeapMemoryUsage().getCommitted());
    }

    public String getMaxMemory() {
        return formatNumber(memory.getHeapMemoryUsage().getMax());
    }

    public String getUsedMemory() {
        return formatNumber(memory.getHeapMemoryUsage().getUsed());
    }

    public String getInitMemory() {
        return formatNumber(memory.getHeapMemoryUsage().getInit());
    }

    public double getCpuLoad() {
        return os.getSystemLoadAverage();
    }

    public int getThreadCount() {
        return threads.getThreadCount();
    }

    public int getPeakThreadCount() {
        return threads.getPeakThreadCount();
    }

    public int getDaemonThreadCount() {
        return threads.getDaemonThreadCount();
    }

    public int getLoadedClassCount() {
        return clb.getLoadedClassCount();
    }

    public long getTotalLoadedClassCount() {
        return clb.getTotalLoadedClassCount();
    }

    public long getUnloadedClassCount() {
        return clb.getUnloadedClassCount();
    }

    public SortedMap<Object, Number> getUsedMemMap() {
        return usedMemMap;
    }

    public SortedMap<Object, Number> getFreeMemMap() {
        return freeMemMap;
    }

    public SortedMap<Object, Number> getCpuMap() {
        return cpuMap;
    }

    @Override
    public synchronized void run() {
        if (usedMemMap.size() > 60) {
            usedMemMap.remove(usedMemMap.firstKey());
            freeMemMap.remove(freeMemMap.firstKey());
            cpuMap.remove(cpuMap.firstKey());
        }
        final Time time = new ChartTime();
        usedMemMap.put(time, memory.getHeapMemoryUsage().getUsed() / Math.pow(1024.0, 2.0));
        freeMemMap.put(time, runtime.freeMemory() / Math.pow(1024.0, 2.0));
        cpuMap.put(time, os.getSystemLoadAverage());
    }
}
