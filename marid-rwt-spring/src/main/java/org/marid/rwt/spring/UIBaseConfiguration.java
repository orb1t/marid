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
import org.springframework.context.annotation.Bean;

public interface UIBaseConfiguration {

  default UIContext uiContext() {
    return new UIBaseContext();
  }

  @Bean
  default UIContext uiContextBean() {
    return uiContext();
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
