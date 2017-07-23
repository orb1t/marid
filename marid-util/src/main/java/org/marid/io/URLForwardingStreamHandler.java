package org.marid.io;

import org.marid.misc.Calls;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.net.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class URLForwardingStreamHandler extends URLStreamHandler {

    protected final URLStreamHandler delegate;

    public URLForwardingStreamHandler(URLStreamHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    protected abstract URLConnection openConnection(URL u) throws IOException;

    @Override
    protected abstract URLConnection openConnection(URL u, Proxy p) throws IOException;

    protected URLConnection obtainConnection(URL u) throws IOException {
        try {
            return Calls.call(() -> {
                final Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                method.setAccessible(true);
                return (URLConnection) method.invoke(delegate, u);
            });
        } catch (UncheckedIOException x) {
            throw x.getCause();
        }
    }

    protected URLConnection obtainConnection(URL u, Proxy p) throws IOException {
        try {
            return Calls.call(() -> {
                final Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
                method.setAccessible(true);
                return (URLConnection) method.invoke(delegate, u, p);
            });
        } catch (UncheckedIOException x) {
            throw x.getCause();
        }
    }

    @Override
    protected void parseURL(URL u, String spec, int start, int limit) {
        Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("parseURL", URL.class, String.class, int.class, int.class);
            method.setAccessible(true);
            return method.invoke(delegate, u, spec, start, limit);
        });
    }

    @Override
    protected int getDefaultPort() {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("getDefaultPort");
            method.setAccessible(true);
            return (int) method.invoke(delegate);
        });
    }

    @Override
    protected boolean equals(URL u1, URL u2) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("equals", URL.class, URL.class);
            method.setAccessible(true);
            return (boolean) method.invoke(delegate, u1, u2);
        });
    }

    @Override
    protected int hashCode(URL u) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("hashCode", URL.class);
            method.setAccessible(true);
            return (int) method.invoke(delegate, u);
        });
    }

    @Override
    protected boolean sameFile(URL u1, URL u2) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("sameFile", URL.class, URL.class);
            method.setAccessible(true);
            return (boolean) method.invoke(delegate, u1, u2);
        });
    }

    @Override
    protected InetAddress getHostAddress(URL u) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("getHostAddress", URL.class);
            method.setAccessible(true);
            return (InetAddress) method.invoke(delegate, u);
        });
    }

    @Override
    protected boolean hostsEqual(URL u1, URL u2) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("hostsEqual", URL.class, URL.class);
            method.setAccessible(true);
            return (boolean) method.invoke(delegate, u1, u2);
        });
    }

    @Override
    protected String toExternalForm(URL u) {
        return Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("toExternalForm", URL.class);
            method.setAccessible(true);
            return (String) method.invoke(delegate, u);
        });
    }

    @Override
    protected void setURL(URL u, String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
        Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class, String.class, String.class, String.class, String.class, String.class);
            method.setAccessible(true);
            return method.invoke(delegate, u, protocol, host, port, authority, userInfo, path, query, ref);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setURL(URL u, String protocol, String host, int port, String file, String ref) {
        Calls.call(() -> {
            final Method method = URLStreamHandler.class.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class, String.class, String.class);
            method.setAccessible(true);
            return method.invoke(delegate, u, protocol, host, port, file, ref);
        });
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
