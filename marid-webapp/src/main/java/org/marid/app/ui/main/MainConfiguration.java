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

import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.marid.app.ui.UIContext;
import org.marid.common.app.spring.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class MainConfiguration {

  @Bean
  @Order(1)
  public Composite header(UIContext context) {
    final Composite header = new Composite(context.getShell(), SWT.NONE);

    final RowLayout layout = new RowLayout();
    layout.spacing = 10;

    header.setLayout(layout);

    return header;
  }

  @Roles({"ROLE_ADMIN"})
  @Bean
  @Order(10)
  public Label adminSection(UIContext context) {
    final Label label = new Label(context.getShell(), SWT.NONE);
    label.setText("Administration");
    return label;
  }

  @Bean
  @Order(11)
  @ConditionalOnBean(name = {"adminSection"})
  public Button users(UIContext context, UrlLauncher urlLauncher) {
    final Button button = new Button(context.getShell(), SWT.NONE);
    button.setText("Users...");
    button.addListener(SWT.Selection, event -> urlLauncher.openURL("users.marid"));
    return button;
  }

  @Autowired
  public void init(JavaScriptExecutor executor) {
    executor.execute("document.title = 'XXX'");
  }
}
