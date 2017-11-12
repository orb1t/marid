/*-
 * #%L
 * marid-proto
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.proto.impl.modbus;

import org.marid.runtime.annotation.MaridBean;

import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
@MaridBean(name = "MODBUS TCP Driver Properties", icon = "D_BUG")
public class ModbusTcpDriverProps {

  private int unitId = 255;
  private int func = 0x03;
  private int count = 1;
  private long period = 1L;
  private long delay = 1L;
  private TimeUnit timeUnit = TimeUnit.SECONDS;
  private int address;
  private long timeout = 1_000L;
  private long errorTimeout = 100L;

  public int getUnitId() {
    return unitId;
  }

  public void setUnitId(int unitId) {
    this.unitId = unitId;
  }

  public int getFunc() {
    return func;
  }

  public void setFunc(int func) {
    this.func = func;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public long getPeriod() {
    return period;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public int getAddress() {
    return address;
  }

  public void setAddress(int address) {
    this.address = address;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public long getErrorTimeout() {
    return errorTimeout;
  }

  public void setErrorTimeout(long errorTimeout) {
    this.errorTimeout = errorTimeout;
  }
}
