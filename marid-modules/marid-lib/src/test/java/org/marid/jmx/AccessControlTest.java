/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.jmx;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MXBean;
import javax.management.ObjectName;
import javax.management.remote.*;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.security.auth.Subject;
import java.rmi.registry.LocateRegistry;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import static javax.management.JMX.newMXBeanProxy;
import static javax.management.remote.JMXConnector.CREDENTIALS;
import static javax.management.remote.JMXConnectorFactory.connect;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
public class AccessControlTest {

    private final Subject monitorSubject = new Subject();
    private final Subject adminSubject = new Subject();

    public AccessControlTest() {
        monitorSubject.getPrincipals().add(new JMXPrincipal("monitor"));
        adminSubject.getPrincipals().add(new JMXPrincipal("admin"));
    }

    @Test
    public void testLocal() {
        final X x = new XImpl();
        final Set<Long> adminSet = Subject.doAs(adminSubject, (PrivilegedAction<Set<Long>>) x::getLongs);
        Assert.assertEquals(ImmutableSet.of(1L), adminSet);
        final Set<Long> monitorSet = Subject.doAs(monitorSubject, (PrivilegedAction<Set<Long>>) x::getLongs);
        Assert.assertEquals(ImmutableSet.of(1L, 2L), monitorSet);
    }

    @Test
    public void testRemoteMonitor() throws Exception {
        LocateRegistry.createRegistry(1099);
        final JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi://localhost:1099/jndi/rmi://:1099/jmxrmi");
        final Map<String, ?> env = ImmutableMap.of(JMXConnectorServer.AUTHENTICATOR, (JMXAuthenticator) credentials -> {
            if (credentials instanceof String) {
                final Subject subject = new Subject();
                switch ((String) credentials) {
                    case "monitor":
                        subject.getPrincipals().add(new JMXPrincipal("monitor"));
                        break;
                }
                return subject;
            } else {
                throw new SecurityException("Invalid credentials: " + credentials);
            }
        });
        final MBeanServer beanServer = MBeanServerFactory.createMBeanServer();
        try {
            beanServer.registerMBean(new XImpl(), new ObjectName("x:y=z"));
            final JMXConnectorServer server = new RMIConnectorServer(serviceURL, env, beanServer);
            server.start();
            try {
                final Map<String, ?> cenv = ImmutableMap.of(CREDENTIALS, "monitor");
                try (final JMXConnector connector = connect(serviceURL, cenv)) {
                    final X x = newMXBeanProxy(connector.getMBeanServerConnection(), new ObjectName("x:y=z"), X.class);
                    Assert.assertEquals(ImmutableSet.of(1L, 2L), x.getLongs());
                }
            } finally {
                server.stop();
            }
        } finally {
            MBeanServerFactory.releaseMBeanServer(beanServer);
        }
    }


    @MXBean
    public interface X {

        Set<Long> getLongs();
    }

    public static class XImpl implements X {

        @Override
        public Set<Long> getLongs() {
            final Subject subject = Subject.getSubject(AccessController.getContext());
            if (subject.getPrincipals().stream().anyMatch(p -> "monitor".equals(p.getName()))) {
                return new TreeSet<>(Arrays.asList(1L, 2L));
            } else {
                return Collections.singleton(1L);
            }
        }
    }
}
