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
public enum ControllerLinkType {
    LAN("LAN"),
    WIFI("Wi-Fi"),
    MOBILE("Wireless Mobile (2G, 2.5G, 3G, 4G)");
    
    private final String label;
    
    private ControllerLinkType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
