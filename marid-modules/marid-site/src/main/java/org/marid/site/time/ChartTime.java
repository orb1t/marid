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
package org.marid.site.time;

import java.sql.Time;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Dmitry Ovchinnikov
 */
public class ChartTime extends Time {
    
    private final int discriminator;
    
    public ChartTime(long time, int discriminator) {
        super(time);
        this.discriminator = discriminator;
    }
    
    public ChartTime(int discriminator) {
        this(System.currentTimeMillis(), discriminator);
    }
    
    public ChartTime() {
        this(10);
    }

    @Override
    public String toString() {
        final Calendar c = new GregorianCalendar();
        c.setTime(this);
        return c.get(Calendar.SECOND) % discriminator == 0 ? super.toString() : " ";
    }
}
