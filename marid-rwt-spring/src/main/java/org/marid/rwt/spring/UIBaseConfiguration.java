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

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.context.annotation.Bean;

public interface UIBaseConfiguration {

  default Display display() {
    return new Display();
  }

  default Shell shell(Display display) {
    final Shell shell = new Shell(display, SWT.NO_TRIM);
    shell.setMaximized(true);
    shell.setLayout(new GridLayout(1, false));
    return shell;
  }

  @Bean
  default SmartFactoryBean<Display> mainDisplayBean() {
    return new SmartFactoryBean<>() {

      private final Display display = display();

      @Override
      public Display getObject() {
        return display;
      }

      @Override
      public Class<Display> getObjectType() {
        return Display.class;
      }

      @Override
      public boolean isEagerInit() {
        return true;
      }
    };
  }

  @Bean
  default SmartFactoryBean<Shell> mainShellBean(Display mainDisplayBean) {
    return new SmartFactoryBean<>() {

      private final Shell shell = shell(mainDisplayBean);

      @Override
      public Shell getObject() {
        return shell;
      }

      @Override
      public Class<Shell> getObjectType() {
        return Shell.class;
      }

      @Override
      public boolean isEagerInit() {
        return true;
      }
    };
  }

  @Bean
  @PrototypeScoped
  default UrlLauncher urlLauncher() {
    return RWT.getClient().getService(UrlLauncher.class);
  }

  @Bean
  @PrototypeScoped
  default JavaScriptExecutor jsExecutor() {
    return RWT.getClient().getService(JavaScriptExecutor.class);
  }
}
