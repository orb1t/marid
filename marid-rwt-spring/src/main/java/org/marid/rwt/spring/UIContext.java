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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.function.BiConsumer;

public interface UIContext {

  Display getDisplay();

  Shell getShell();

  default void configure(BiConsumer<Display, Shell> consumer) {
    consumer.accept(getDisplay(), getShell());
  }
}
