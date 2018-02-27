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

import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.swt.widgets.Shell;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.stream.Stream;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RwtConfiguration.class})
@ComponentScan(excludeFilters = {@Filter(type = FilterType.CUSTOM, classes = {UIExcludeFilter.class})})
public @interface EnableRwt {

  Class<? extends UIBaseConfiguration> baseConfigurationClass();
}

class RwtConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServletContextListener rwtServletContextListener(GenericApplicationContext context) {
    final EnableRwt enableRwt = Stream.of(context.getBeanNamesForAnnotation(EnableRwt.class))
        .map(beanName -> context.findAnnotationOnBean(beanName, EnableRwt.class))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No beans with annotation " + EnableRwt.class.getName()));

    return new ServletContextListener() {

      private ApplicationRunner runner;

      @Override
      public void contextInitialized(ServletContextEvent sce) {
        runner = new ApplicationRunner(application -> {
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
              child.register(enableRwt.baseConfigurationClass(), endPoint.getConfigurationClass());
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
  @ConditionalOnMissingBean
  public ServletRegistrationBean<RWTServlet> rwtServletBean() {
    final RWTServlet servlet = new RWTServlet();
    final ServletRegistrationBean<RWTServlet> bean = new ServletRegistrationBean<>(servlet, "*.marid");
    bean.setName("rwtServlet");
    bean.setAsyncSupported(false);
    bean.setOrder(1);
    return bean;
  }
}

class UIExcludeFilter implements TypeFilter, BeanFactoryAware {

  private String pkg;

  @Override
  public boolean match(@NotNull MetadataReader metadataReader, @NotNull MetadataReaderFactory metadataReaderFactory) {
    return pkg != null && metadataReader.getClassMetadata().getClassName().startsWith(pkg);
  }

  @Override
  public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
    if (beanFactory instanceof ListableBeanFactory) {
      final ListableBeanFactory factory = (ListableBeanFactory) beanFactory;
      pkg = Stream.of(factory.getBeanNamesForAnnotation(EnableRwt.class))
          .map(beanName -> factory.findAnnotationOnBean(beanName, EnableRwt.class))
          .filter(Objects::nonNull)
          .map(EnableRwt::baseConfigurationClass)
          .map(Class::getPackage)
          .map(Package::getName)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("No beans with annotation " + EnableRwt.class.getName()));
    }
  }
}
