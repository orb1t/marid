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

package org.marid.common.app.util;

import org.jetbrains.annotations.NotNull;
import org.marid.l10n.L10n;
import org.springframework.context.i18n.LocaleContextHolder;

public interface UILocalization {

  @NotNull
  static String ls(@NotNull String value, Object... ps) {
    return L10n.s(LocaleContextHolder.getLocale(), value, ps);
  }

  @NotNull
  static String lm(@NotNull String value, Object... ps) {
    return L10n.m(LocaleContextHolder.getLocale(), value, ps);
  }
}
