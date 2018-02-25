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

package org.marid.app.ui.main;

import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.marid.app.ui.UIContext;
import org.marid.common.app.l10n.LMain;
import org.marid.common.app.spring.Roles;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static org.eclipse.swt.SWT.*;
import static org.eclipse.swt.layout.GridData.FILL_HORIZONTAL;

@Configuration
public class MainConfiguration {

  @Roles({"ROLE_ADMIN"})
  @Bean
  @Order(10)
  public Group adminSection(UIContext context) {
    final Group group = new Group(context.getShell(), SHADOW_ETCHED_IN);
    group.setLayout(new RowLayout());
    group.setText(LMain.get().admin);
    group.setLayoutData(new GridData(FILL_HORIZONTAL));
    return group;
  }

  @Bean
  @Order(20)
  public Group sessionSection(UIContext context) {
    final Group group = new Group(context.getShell(), SHADOW_ETCHED_IN);
    group.setLayout(new RowLayout());
    group.setText(LMain.get().session);
    group.setLayoutData(new GridData(FILL_HORIZONTAL));
    return group;
  }

  @Bean
  @Order(1)
  @ConditionalOnBean(name = "adminSection")
  public Button users(Group adminSection, UrlLauncher urlLauncher) {
    final Button button = new Button(adminSection, PUSH);
    button.setText(LMain.get().users);
    button.addListener(Selection, event -> urlLauncher.openURL("users.marid"));
    return button;
  }

  @Bean
  @Order(1)
  public Button logout(Group sessionSection, UrlLauncher urlLauncher) {
    final Button button = new Button(sessionSection, PUSH);
    button.setText(LMain.get().logout);
    button.addListener(Selection, event -> urlLauncher.openURL("login?logout"));
    return button;
  }
}
