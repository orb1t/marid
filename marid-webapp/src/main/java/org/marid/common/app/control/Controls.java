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

package org.marid.common.app.control;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.function.Consumer;

import static org.eclipse.swt.SWT.NONE;

public interface Controls {

  @SafeVarargs
  static void label(Composite parent, String label, Consumer<Label>... confs) {
    final Label l = new Label(parent, NONE);
    l.setText(label);

    for (final Consumer<Label> conf : confs) {
      conf.accept(l);
    }
  }
}
