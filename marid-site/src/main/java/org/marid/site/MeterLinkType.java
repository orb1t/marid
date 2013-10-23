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

/**
 * @author Dmitry Ovchinnikov
 */
public enum MeterLinkType {
    RS485_CAN("RS-485/CAN"),
    LAN("LAN"),
    ZIGBEE("Zigbee"),
    WIFI("Wi-Fi");
    
    private final String label;
    
    private MeterLinkType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}