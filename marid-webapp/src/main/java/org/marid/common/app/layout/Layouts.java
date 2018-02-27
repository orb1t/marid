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

package org.marid.common.app.layout;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.intellij.lang.annotations.MagicConstant;

import java.util.function.Consumer;

import static org.eclipse.swt.SWT.HORIZONTAL;
import static org.eclipse.swt.SWT.VERTICAL;

public interface Layouts {

  int MARGIN = 12;
  int SPACING = 12;

  @SafeVarargs
  static GridLayout grid(int columns, boolean equalWidth, Consumer<GridLayout>... confs) {
    final GridLayout layout = new GridLayout(columns, equalWidth);
    layout.marginWidth = layout.marginHeight = MARGIN;
    layout.horizontalSpacing = layout.verticalSpacing = SPACING;

    for (final Consumer<GridLayout> conf : confs) {
      conf.accept(layout);
    }

    return layout;
  }

  @SafeVarargs
  static RowLayout row(@MagicConstant(intValues = {HORIZONTAL, VERTICAL}) int type, Consumer<RowLayout>... confs) {
    final RowLayout layout = new RowLayout(type);

    layout.marginWidth = layout.marginHeight = MARGIN;
    layout.spacing = SPACING;

    for (final Consumer<RowLayout> conf : confs) {
      conf.accept(layout);
    }

    return layout;
  }

  @SafeVarargs
  static FillLayout fill(@MagicConstant(intValues = {HORIZONTAL, VERTICAL}) int type, Consumer<FillLayout>... confs) {
    final FillLayout layout = new FillLayout(type);

    layout.marginWidth = layout.marginHeight = MARGIN;
    layout.spacing = SPACING;

    for (final Consumer<FillLayout> conf : confs) {
      conf.accept(layout);
    }

    return layout;
  }

  @SafeVarargs
  static StackLayout stack(Consumer<StackLayout>... confs) {
    final StackLayout layout = new StackLayout();

    layout.marginWidth = layout.marginHeight = MARGIN;

    for (final Consumer<StackLayout> conf : confs) {
      conf.accept(layout);
    }

    return layout;
  }

  @SafeVarargs
  static FormLayout form(Consumer<FormLayout>... confs) {
    final FormLayout layout = new FormLayout();

    layout.marginWidth = layout.marginHeight = MARGIN;
    layout.spacing = SPACING;

    for (final Consumer<FormLayout> conf : confs) {
      conf.accept(layout);
    }

    return layout;
  }
}
