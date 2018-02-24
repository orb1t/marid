/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

package org.marid.app.config;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.swt.widgets.Shell;
import org.marid.app.ui.UIBaseConfiguration;
import org.marid.app.ui.UIContext;
import org.marid.app.ui.UIContextInitializer;
import org.marid.common.app.endpoint.EndPoint;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

import static org.marid.common.app.util.UILocalization.ls;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

@Configuration
public class ServletConfiguration {

  @Bean
  public ServletContextListener rwtServletContextListener(Map<String, EndPoint> endPoints, GenericApplicationContext context) {
    return new ServletContextListener() {

      private ApplicationRunner runner;

      @Override
      public void contextInitialized(ServletContextEvent sce) {
        runner = new ApplicationRunner(application -> {
          application.setOperationMode(Application.OperationMode.JEE_COMPATIBILITY);

          endPoints.forEach((name, endPoint) -> {
            final EntryPointFactory entryPointFactory = () -> () -> {
              final AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
              child.setParent(context);
              child.setDisplayName(endPoint.getPath());
              child.setId(endPoint.getPath());
              child.setAllowBeanDefinitionOverriding(false);
              child.setAllowCircularReferences(false);
              child.register(UIBaseConfiguration.class, endPoint.getConfigurationClass());
              child.refresh();
              child.start();

              try {
                final UIContext uiContext = child.getBean(UIContext.class);
                child.getBean(UIContextInitializer.class).initialize(uiContext);

                final Shell shell = uiContext.getShell();

                if (shell.getMaximized()) {
                  shell.layout();
                } else {
                  shell.pack();
                }

                shell.addDisposeListener(event -> child.close());
                shell.open();

                final JavaScriptExecutor jsExecutor = child.getBean(JavaScriptExecutor.class);
                jsExecutor.execute(String.format("document.title = '%s'", escapeJavaScript(ls(name))));
              } catch (Throwable x) {
                child.close();
                throw x;
              }
              return 0;
            };

            application.addEntryPoint(endPoint.getPath(), entryPointFactory, endPoint.getParameters());
          });
        }, sce.getServletContext());
        runner.start();
      }

      @Override
      public void contextDestroyed(ServletContextEvent sce) {
        if (runner != null) {
          runner.stop();
          runner = null;
        }
      }
    };
  }

  @Bean
  public ServletRegistrationBean<RWTServlet> rwtServletBean() {
    final RWTServlet servlet = new RWTServlet();
    final ServletRegistrationBean<RWTServlet> bean = new ServletRegistrationBean<>(servlet, "*.marid");
    bean.setName("rwtServlet");
    bean.setAsyncSupported(false);
    bean.setOrder(1);
    return bean;
  }
}
