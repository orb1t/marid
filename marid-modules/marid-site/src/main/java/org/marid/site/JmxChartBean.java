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
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class JmxChartBean implements Serializable {
    
    @ManagedProperty("#{jmxBean}")
    private JmxBean jmxBean;
    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;
    private CartesianChartModel memModel;
    private ChartSeries usedMemSeries;
    private ChartSeries freeMemSeries;
    private CartesianChartModel cpuModel;
    private ChartSeries cpuSeries;

    public void setJmxBean(JmxBean jmxBean) {
        this.jmxBean = jmxBean;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }
    
    @PostConstruct
    public void init() {
        memModel = new CartesianChartModel();
        cpuModel = new CartesianChartModel();
        memModel.addSeries(usedMemSeries = new ChartSeries(localeBean.msg("Used memory")));
        usedMemSeries.setData(jmxBean.getUsedMemMap());
        memModel.addSeries(freeMemSeries = new ChartSeries(localeBean.msg("Free memory")));
        freeMemSeries.setData(jmxBean.getFreeMemMap());
        cpuModel.addSeries(cpuSeries = new ChartSeries(localeBean.msg("CPU load")));
        cpuSeries.setData(jmxBean.getCpuMap());
    }

    public CartesianChartModel getMemModel() {
        return memModel;
    }

    public CartesianChartModel getCpuModel() {
        return cpuModel;
    }
}
