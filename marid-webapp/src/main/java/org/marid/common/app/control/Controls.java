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

package org.marid.common.app.control;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.eclipse.swt.SWT.NONE;

public interface Controls {

  String FILLED_CIRCLE = "\u25CF";

  @SafeVarargs
  static Label label(Composite parent, String label, Consumer<Label>... confs) {
    final Label l = new Label(parent, NONE);
    l.setText(label);

    for (final Consumer<Label> conf : confs) {
      conf.accept(l);
    }

    return l;
  }

  @SafeVarargs
  static TableColumn column(Table table, String text, Consumer<TableColumn>... confs) {
    final TableColumn c = new TableColumn(table, NONE);
    c.setText(text);

    for (final Consumer<TableColumn> conf : confs) {
      conf.accept(c);
    }

    return c;
  }

  static void autoResize(Table table, int... weights) {
    final int sum = IntStream.of(weights).sum();
    final Runnable resizeTask = () -> {
      final TableColumn[] columns = table.getColumns();
      final Rectangle clientArea = table.getClientArea();

      for (int i = 0; i < columns.length; i++) {
        columns[i].setWidth((clientArea.width * weights[i]) / sum);
      }
    };
    table.addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        table.removeControlListener(this);

        try {
          resizeTask.run();
        } finally {
          table.addControlListener(this);
        }
      }
    });
  }
}
