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

/**
 * @author Dmitry Ovchinnikov
 */
public class SecureContext {

    private static final SSLContext SSL_CONTEXT;
    private static final SSLServerSocketFactory SERVER_SOCKET_FACTORY;
    private static final SSLSocketFactory SOCKET_FACTORY;
    private static final Exception SSL_CONTEXT_EXCEPTION;
    private static final KeyStore KEY_STORE;

    static {
        Exception exception;
        SSLContext ctx;
        SSLServerSocketFactory ssf;
        SSLSocketFactory sf;
        KeyStore ks;
        try {
            final String sslContextProtocol = ParseUtils.getString("MW_SSL_CONTEXT_PROTOCOL", "TLS");
            final String sslContextProvider = ParseUtils.getString("MW_SSL_CONTEXT_PROVIDER", null);
            ctx = sslContextProvider == null
                    ? SSLContext.getInstance(sslContextProtocol)
                    : SSLContext.getInstance(sslContextProtocol, sslContextProvider);
            final String keyStoreProvider = ParseUtils.getString("MW_KS_PROVIDER", null);
            final String keyStoreType = ParseUtils.getString("MW_KS_TYPE", KeyStore.getDefaultType());
            ks = keyStoreProvider == null
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
            exception = null;
            ssf = ctx.getServerSocketFactory();
            sf = ctx.getSocketFactory();
        } catch (Exception x) {
            ks = null;
            ctx = null;
            ssf = null;
            sf = null;
            exception = x;
        }
        KEY_STORE = ks;
        SSL_CONTEXT = ctx;
        SOCKET_FACTORY = sf;
        SERVER_SOCKET_FACTORY = ssf;
        SSL_CONTEXT_EXCEPTION = exception;
    }

    public static SSLServerSocketFactory getServerSocketFactory() {
        if (SERVER_SOCKET_FACTORY != null) {
            return SERVER_SOCKET_FACTORY;
        } else {
            throw new IllegalStateException("Server socket factory error", SSL_CONTEXT_EXCEPTION);
        }
    }

    public static SSLSocketFactory getSocketFactory() {
        if (SOCKET_FACTORY != null) {
            return SOCKET_FACTORY;
        } else {
            throw new IllegalStateException("Socket factory error", SSL_CONTEXT_EXCEPTION);
        }
    }

    public static SSLContext getSslContext() {
        if (SSL_CONTEXT != null) {
            return SSL_CONTEXT;
        } else {
            throw new IllegalStateException("SSL context error", SSL_CONTEXT_EXCEPTION);
        }
    }

    public static KeyStore getKeyStore() {
        if (KEY_STORE != null) {
            return KEY_STORE;
        } else {
            throw new IllegalStateException("Key store error", SSL_CONTEXT_EXCEPTION);
        }
    }
}
