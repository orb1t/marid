/*-
 * #%L
 * marid-rwt-spring-boot
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

package org.marid.rwt.spring;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.swt.widgets.Shell;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RwtConfiguration.class})
public @interface EnableRwt {

  Class<? extends UIBaseConfiguration> value();
}

class RwtConfiguration {

  @Bean
  public ServletContextListener rwtServletContextListener(GenericApplicationContext context) {
    final EnableRwt rwt = Stream.of(context.getBeanNamesForAnnotation(EnableRwt.class))
        .map(beanName -> context.findAnnotationOnBean(beanName, EnableRwt.class))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No beans with annotation " + EnableRwt.class.getName()));
    return new ServletContextListener() {

      private ApplicationRunner runner;

      @Override
      public void contextInitialized(ServletContextEvent sce) {
        runner = new ApplicationRunner(application -> {
          application.setOperationMode(Application.OperationMode.JEE_COMPATIBILITY);
          context.getBeansOfType(ApplicationConfigurer.class).forEach((n, c) -> {
            try {
              c.configure(application);
            } catch (RuntimeException x) {
              throw new IllegalStateException(String.format("[%s]: Unable to configure application", n), x);
            }
          });
          context.getBeansOfType(EndPoint.class).forEach((name, endPoint) -> {
            final EntryPointFactory entryPointFactory = () -> () -> {
              final AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();

              child.setParent(context);
              child.setDisplayName(name);
              child.setId(name);
              child.setAllowBeanDefinitionOverriding(false);
              child.setAllowCircularReferences(false);
              child.register(rwt.value(), endPoint.getConfigurationClass());
              child.getBeanFactory().registerSingleton("endPoint", endPoint);
              child.refresh();
              child.start();

              try {
                final UIContext uiContext = child.getBean(UIContext.class);

                final Shell shell = uiContext.getShell();
                shell.setData("MARID_END_POINT_NAME", name);

                if (shell.getMaximized()) {
                  shell.layout();
                } else {
                  shell.pack();
                }

                shell.addDisposeListener(event -> child.close());
                shell.open();
              } catch (Throwable x) {
                child.close();
                throw x;
              }
              return 0;
            };

            context.getBeansOfType(EndPointConfigurer.class).forEach((n, c) -> {
              try {
                c.configure(name, endPoint);
              } catch (RuntimeException x) {
                throw new IllegalStateException(String.format("[%s]: Unable to configure %s", n, name), x);
              }
            });

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
