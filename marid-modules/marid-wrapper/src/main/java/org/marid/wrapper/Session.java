/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.wrapper;

import org.marid.wrapper.deploy.ClientInfo;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.info;
import static org.marid.wrapper.Log.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Session implements Runnable {

    private static final Logger LOG = Logger.getLogger(Session.class.getName());

    final SSLSocket socket;
    final SSLSession session;

    public Session(SSLSocket socket, SSLSession session) {
        this.socket = socket;
        this.session = session;
    }

    private void doRun(ClientInfo ci, DataInputStream is, DataOutputStream os) throws Exception {
        is.readFully(new byte[3]);
    }

    @Override
    public void run() {
        final ClientInfo clientInfo = new ClientInfo(socket.getInetAddress());
        try (final DataInputStream i = new DataInputStream(socket.getInputStream());
             final DataOutputStream o = new DataOutputStream(socket.getOutputStream())) {
            doRun(clientInfo, i, o);
        } catch (Exception x) {
            warning(LOG, "{0} session error", x, this);
        } finally {
            try {
                socket.close();
                info(LOG, "{0} Closed", socket);
            } catch (Exception x) {
                warning(LOG, "{0} Unable to close", x, socket);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
