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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class UIBaseContext implements UIContext {

  private final Display display;
  private final Shell shell;

  public UIBaseContext() {
    this.display = new Display();
    this.shell = new Shell(display, SWT.NO_TRIM);
    this.shell.setMaximized(true);
    this.shell.setLayout(new GridLayout(1, false));
  }

  @Override
  public Display getDisplay() {
    return display;
  }

  @Override
  public Shell getShell() {
    return shell;
  }
}
