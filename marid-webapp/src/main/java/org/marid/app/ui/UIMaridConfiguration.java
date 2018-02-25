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

package org.marid.app.ui;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.marid.rwt.spring.UIBaseConfiguration;
import org.marid.rwt.spring.UIContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.marid.common.app.util.UILocalization.ls;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

@Configuration
@Import({UILoggingInitializer.class})
public class UIMaridConfiguration implements UIBaseConfiguration {

  @Override
  public UIContext uiContext() {
    final UIContext context = UIBaseConfiguration.super.uiContext();
    final GridLayout layout = (GridLayout) context.getShell().getLayout();
    layout.marginTop = layout.marginBottom = layout.marginLeft = layout.marginRight = 10;
    context.getShell().addShellListener(new ShellAdapter() {
      @Override
      public void shellActivated(ShellEvent e) {
        final JavaScriptExecutor jsExecutor = RWT.getClient().getService(JavaScriptExecutor.class);
        final Shell shell = context.getShell();
        final String id = shell.getData("MARID_END_POINT_NAME").toString();
        jsExecutor.execute(String.format("document.title = '%s'", escapeJavaScript(ls(id))));
        shell.removeShellListener(this);
      }
    });
    return context;
  }
}
