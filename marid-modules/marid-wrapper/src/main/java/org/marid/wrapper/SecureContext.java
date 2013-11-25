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

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SecureContext {

    private static final Logger LOG = Logger.getLogger(SecureContext.class.getName());
    private static final SSLContext SSL_CONTEXT;

    static {
        SSLContext ctx;
        try {
            final String sslContextProtocol = ParseUtils.getString("MW_SSL_CONTEXT_PROTOCOL", "TLS");
            final String sslContextProvider = ParseUtils.getString("MW_SSL_CONTEXT_PROVIDER", null);
            ctx = sslContextProvider == null
                    ? SSLContext.getInstance(sslContextProtocol)
                    : SSLContext.getInstance(sslContextProtocol, sslContextProvider);
            final String keyStoreProvider = ParseUtils.getString("MW_KS_PROVIDER", null);
            final String keyStoreType = ParseUtils.getString("MW_KS_TYPE", KeyStore.getDefaultType());
            final KeyStore ks = keyStoreProvider == null
                    ? KeyStore.getInstance(keyStoreType)
                    : KeyStore.getInstance(keyStoreType, keyStoreProvider);
            final String keyStoreFile = ParseUtils.getString("MW_KS_FILE", null);
            final String keyStorePassword = ParseUtils.getString("MW_KS_PASSWORD", null);
            final char[] storePassword;
            if (keyStoreFile != null && keyStorePassword != null) {
                try (final InputStream inputStream = Files.newInputStream(Paths.get(keyStoreFile))) {
                    ks.load(inputStream, storePassword = keyStorePassword.toCharArray());
                }
            } else {
                try (final InputStream inputStream = SecureContext.class.getResourceAsStream("marid.jks")) {
                    ks.load(inputStream, storePassword = new char[] {'c', 'u', 'c', 'u', 'c', 'u'});
                }
            }
            final String kmfAlg = ParseUtils.getString("MW_KMF_ALG", KeyManagerFactory.getDefaultAlgorithm());
            final String kmfProvider = ParseUtils.getString("MW_KMF_PROVIDER", null);
            final KeyManagerFactory kmf = kmfProvider == null
                    ? KeyManagerFactory.getInstance(kmfAlg)
                    : KeyManagerFactory.getInstance(kmfAlg, kmfProvider);
            final String keyPassword = ParseUtils.getString("MW_KEY_PASSWORD", null);
            kmf.init(ks, keyPassword == null ? storePassword : keyPassword.toCharArray());
            final String tmfAlg = ParseUtils.getString("MW_TMF_ALG", TrustManagerFactory.getDefaultAlgorithm());
            final String tmfProvider = ParseUtils.getString("MW_MTF_PROVIDER", null);
            final TrustManagerFactory tmf = tmfProvider == null
                    ? TrustManagerFactory.getInstance(tmfAlg)
                    : TrustManagerFactory.getInstance(tmfAlg, tmfProvider);
            tmf.init(ks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception x) {
            severe(LOG, "Unable to get SSL context", x);
            ctx = null;
        }
        SSL_CONTEXT = ctx;
    }

    public static final SSLServerSocketFactory SERVER_SOCKET_FACTORY = SSL_CONTEXT == null
            ? null
            : SSL_CONTEXT.getServerSocketFactory();

    public static final SSLSocketFactory SOCKET_FACTORY = SSL_CONTEXT == null
            ? null
            : SSL_CONTEXT.getSocketFactory();
}
