/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.beaneditor.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.marid.dependant.beaneditor.actions.BeanActionManager;
import org.marid.expression.mutable.*;
import org.marid.idelib.beans.IdeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static org.marid.collections.MaridIterators.forEach;

@Component
public class IdeBeanViewFactory {

  private final BeanActionManager actionManager;

  @Autowired
  public IdeBeanViewFactory(BeanActionManager actionManager) {
    this.actionManager = actionManager;
  }

  @Nonnull
  public HBox createView(@Nonnull IdeBean bean, @Nonnull Expr expr) {
    final HBox tf = new HBox();
    tf.setAlignment(Pos.CENTER_LEFT);
    tf.setFillHeight(true);
    createView(bean, expr, tf);
    return tf;
  }

  private void createView(@Nonnull IdeBean bean, @Nonnull Expr expr, @Nonnull HBox tf) {
    if (expr instanceof CallExpr) {
      createView(bean, (CallExpr) expr, tf);
    } else if (expr instanceof GetExpr) {
      createView(bean, (GetExpr) expr, tf);
    } else if (expr instanceof SetExpr) {
      createView(bean, (SetExpr) expr, tf);
    } else if (expr instanceof ArrayExpr) {
      createView(bean, (ArrayExpr) expr, tf);
    } else if (expr instanceof ClassExpr) {
      createView((ClassExpr) expr, tf);
    } else if (expr instanceof StringExpr) {
      createView((StringExpr) expr, tf);
    } else if (expr instanceof RefExpr) {
      createView((RefExpr) expr, tf);
    }
  }

  private void createView(@Nonnull IdeBean bean, @Nonnull CallExpr expr, @Nonnull HBox tf) {
    createView(bean, expr.getTarget(), tf);
    tf.getChildren().add(new Label("." + expr.getMethod() + "("));
    forEach(expr.getArgs(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Label(", "));
      }
      createView(bean, e, tf);
    });
    tf.getChildren().add(new Label(")"));
  }

  private void createView(@Nonnull IdeBean bean, @Nonnull GetExpr expr, @Nonnull HBox tf) {
    createView(bean, expr.getTarget(), tf);
    tf.getChildren().add(new Label("." + expr.getField()));
  }

  private void createView(@Nonnull IdeBean bean, @Nonnull SetExpr expr, @Nonnull HBox tf) {
    createView(bean, expr.getTarget(), tf);
    tf.getChildren().add(new Label("." + expr.getField() + "="));
    createView(bean, expr.getValue(), tf);
  }

  private void createView(@Nonnull IdeBean bean, @Nonnull ArrayExpr expr, @Nonnull HBox tf) {
    forEach(expr.getElements(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Label(", "));
      }
      createView(bean, e, tf);
    });
  }

  private void createView(@Nonnull ClassExpr expr, @Nonnull HBox tf) {
    final Label text = new Label(BeanViewUtils.replaceQualified(expr.getClassName()));
    text.setTooltip(new Tooltip(expr.getClassName()));
    tf.getChildren().add(text);
  }

  private void createView(@Nonnull StringExpr expr, @Nonnull HBox tf) {
    tf.getChildren().add(new Label(expr.getValue()));
  }

  private void createView(@Nonnull RefExpr expr, @Nonnull HBox tf) {
    tf.getChildren().add(new Label(expr.getReference()));
  }
}
