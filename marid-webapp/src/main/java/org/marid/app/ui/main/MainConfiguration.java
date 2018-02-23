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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.marid.app.ui.UIContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfiguration {

  @Bean
  public Label label1(UIContext context) {
    final Label label = new Label(context.getShell(), SWT.NONE);
    label.setText("TXT");
    return label;
  }

  @Autowired
  public void initContext(UIContext context) {
    context.configure(((display, shell) -> {
      shell.setMaximized(true);
      shell.setLayout(new GridLayout(1, false));
    }));
  }
}
