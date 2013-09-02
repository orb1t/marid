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
package org.marid.site.model;

/**
 * @author Dmitry Ovchinnikov
 */
public enum Advantage {
    BINARY_PROTOCOL_PARSER("bpp.png", "Binary protocol parser with graphical configurer", "adv/bpp.xhtml"),
    DEVICE_PROTOCOL_EMULATOR("dpe.png", "Device protocol emulator with graphical configurer", "adv/dpe.xhtml"),
    REALTIME_TASK_SCHEDULER("rts.png", "Realtime task scheduler with graphical configurer", "adv/rts.xhtml"),
    IDE("ide.png", "Integrated graphical environment", "adv/ide.xhtml"),
    HTTP_SERVER("http.png", "Embedded HTTP/HTTPS server with graphical configurer", "adv/http.xhtml"),
    THICK_GRAPHICAL_CLIENTS("thick.png", "Thick graphical clients (SWING & JafaFX based)", "adv/thick.png"),
    CROSS_PLATFORM("cp.png", "Cross-platform (written in Java)", "adv/cp.xhtml"),
    EMBEDDED_DBMS("db.png", "Embedded database with bottom-to-top data synchronization", "adv/db.xhtml"),
    REMOTE_NODE_MANAGEMENT("rcm.png", "Remote node control and monitoring (logs, JMX)", "adv/rnm.xhtml"),
    REMOTE_SCRIPT_DEBUGGING("rsd.png", "Remote script debugging", "adv/rsd.xhtml"),
    DEPLOY_TO_MULTIPLE_NODES("deploy.png", "Deploy to multiple nodes from the one point", "adv/deploy.xhtml"),
    HIGH_DATA_TRANSFER_RELIABILITY("data.png", "High data transfer reliability on slow communication lines", "adv/data.xhtml");
        
    private final String icon;
    private final String text;
    private final String link;
        
    private Advantage(String icon, String text, String link) {
        this.icon = icon;
        this.text = text;
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }
}
