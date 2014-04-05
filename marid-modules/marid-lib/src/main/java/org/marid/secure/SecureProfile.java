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

package org.marid.secure;

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class SecureProfile {

    public static final SecureProfile DEFAULT = new SecureProfile(getProperties());

    private final KeyStore keyStore;
    private final SSLContext sslContext;
    private final SSLServerSocketFactory serverSocketFactory;
    private final SSLSocketFactory socketFactory;
    private final Exception exception;

    public SecureProfile(Class<?> baseClass, Properties props) {
        Exception ex;
        SSLContext ctx;
        SSLServerSocketFactory ssf;
        SSLSocketFactory sf;
        KeyStore ks;
        try {
            final String sslContextProtocol = props.getProperty("ssl.context.protocol", "TLS");
            final String sslContextProvider = props.getProperty("ssl.context.provider", null);
            ctx = sslContextProvider == null
                    ? SSLContext.getInstance(sslContextProtocol)
                    : SSLContext.getInstance(sslContextProtocol, sslContextProvider);
            final String keyStoreProvider = props.getProperty("key.store.provider", null);
            final String keyStoreType = props.getProperty("key.store.type", KeyStore.getDefaultType());
            ks = keyStoreProvider == null
                    ? KeyStore.getInstance(keyStoreType)
                    : KeyStore.getInstance(keyStoreType, keyStoreProvider);
            final String keyStoreFile = props.getProperty("key.store.file", null);
            final String keyStorePassword = props.getProperty("key.store.password", null);
            final char[] storePassword;
            if (keyStoreFile != null && keyStorePassword != null) {
                try (final InputStream inputStream = Files.newInputStream(Paths.get(keyStoreFile))) {
                    ks.load(inputStream, storePassword = keyStorePassword.toCharArray());
                }
            } else {
                try (final InputStream inputStream = baseClass.getResourceAsStream("marid.jks")) {
                    ks.load(inputStream, storePassword = new char[]{'m', 'a', 'r', 'i', 'd', 'o'});
                }
            }
            final String kmfAlg = props.getProperty("key.manager.factory.algorithm",
                    KeyManagerFactory.getDefaultAlgorithm());
            final String kmfProvider = props.getProperty("key.manager.factory.provider", null);
            final KeyManagerFactory kmf = kmfProvider == null
                    ? KeyManagerFactory.getInstance(kmfAlg)
                    : KeyManagerFactory.getInstance(kmfAlg, kmfProvider);
            final String keyPassword = props.getProperty("key.manager.factory.password", String.valueOf(storePassword));
            kmf.init(ks, keyPassword.toCharArray());
            final String tmfAlg = props.getProperty("trust.manager.factory.algorithm",
                    TrustManagerFactory.getDefaultAlgorithm());
            final String tmfProvider = props.getProperty("trust.manager.factory.provider", null);
            final TrustManagerFactory tmf = tmfProvider == null
                    ? TrustManagerFactory.getInstance(tmfAlg)
                    : TrustManagerFactory.getInstance(tmfAlg, tmfProvider);
            tmf.init(ks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            ssf = ctx.getServerSocketFactory();
            sf = ctx.getSocketFactory();
            ex = null;
        } catch (Exception x) {
            ex = x;
            ks = null;
            sf = null;
            ssf = null;
            ctx = null;
        }
        keyStore = ks;
        sslContext = ctx;
        serverSocketFactory = ssf;
        socketFactory = sf;
        exception = ex;
    }

    public SecureProfile(Properties properties) {
        this(SecureProfile.class, properties);
    }

    private static Properties getProperties() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = classLoader.getResourceAsStream("maridSecurity.properties")) {
            final Properties properties = new Properties();
            if (is != null) {
                properties.load(is);
            }
            return properties;
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    private void checkException() {
        if (exception != null) {
            throw new SecurityException(exception);
        }
    }

    public KeyStore getKeyStore() {
        checkException();
        return keyStore;
    }

    public SSLContext getSslContext() {
        checkException();
        return sslContext;
    }

    public SSLServerSocketFactory getServerSocketFactory() {
        checkException();
        return serverSocketFactory;
    }

    public SSLSocketFactory getSocketFactory() {
        checkException();
        return socketFactory;
    }
}
