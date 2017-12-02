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

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.marid.dependant.beaneditor.actions.BeanActionManager;
import org.marid.expression.mutable.*;
import org.marid.idelib.beans.IdeBean;
import org.marid.jfx.icons.FontIcons;
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
  public TextFlow createView(@Nonnull IdeBean bean, @Nonnull Expr expr) {
    final TextFlow tf = new TextFlow();
    tf.setPrefWidth(Region.USE_COMPUTED_SIZE);
    if (bean.getParent() == null && bean.getFactory() == expr) {
      tf.getChildren().add(FontIcons.glyphIcon("D_ROOMBA"));
    } else {
      tf.getChildren().add(editButton(bean, expr));
      createView(0, bean, expr, tf);
    }
    return tf;
  }

  private void createView(int level, @Nonnull IdeBean bean, @Nonnull Expr expr, @Nonnull TextFlow tf) {
    if (level < 3) {
      if (expr instanceof CallExpr) {
        createView(level, bean, (CallExpr) expr, tf);
      } else if (expr instanceof GetExpr) {
        createView(level, bean, (GetExpr) expr, tf);
      } else if (expr instanceof SetExpr) {
        createView(level, bean, (SetExpr) expr, tf);
      } else if (expr instanceof ArrayExpr) {
        createView(level, bean, (ArrayExpr) expr, tf);
      } else if (expr instanceof ClassExpr) {
        createView((ClassExpr) expr, tf);
      } else if (expr instanceof StringExpr) {
        createView((StringExpr) expr, tf);
      } else if (expr instanceof RefExpr) {
        createView((RefExpr) expr, tf);
      }
    }
  }

  private void createView(int level, @Nonnull IdeBean bean, @Nonnull CallExpr expr, @Nonnull TextFlow tf) {
    tf.getChildren().add(editInitializersButton(bean, expr));
    createView(level + 1, bean, expr.getTarget(), tf);
    tf.getChildren().add(new Text("." + expr.getMethod() + "("));
    forEach(expr.getArgs(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Text(", "));
      }
      createView(level + 1, bean, e, tf);
    });
    tf.getChildren().add(new Text(")"));
  }

  private void createView(int level, @Nonnull IdeBean bean, @Nonnull GetExpr expr, @Nonnull TextFlow tf) {
    tf.getChildren().add(editInitializersButton(bean, expr));
    createView(level + 1, bean, expr.getTarget(), tf);
    tf.getChildren().add(new Text("." + expr.getField()));
  }

  private void createView(int level, @Nonnull IdeBean bean, @Nonnull SetExpr expr, @Nonnull TextFlow tf) {
    createView(level + 1, bean, expr.getTarget(), tf);
    tf.getChildren().add(new Text("." + expr.getField() + "="));
    createView(level + 1, bean, expr.getValue(), tf);
  }

  private void createView(int level, @Nonnull IdeBean bean, @Nonnull ArrayExpr expr, @Nonnull TextFlow tf) {
    forEach(expr.getElements(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Text(", "));
      }
      createView(level + 1, bean, e, tf);
    });
  }

  private void createView(@Nonnull ClassExpr expr, @Nonnull TextFlow tf) {
    final Text text = new Text(BeanViewUtils.replaceQualified(expr.getClassName()));
    Tooltip.install(text, new Tooltip(expr.getClassName()));
    tf.getChildren().add(text);
  }

  private void createView(@Nonnull StringExpr expr, @Nonnull TextFlow tf) {
    tf.getChildren().add(new Text(expr.getValue()));
  }

  private void createView(@Nonnull RefExpr expr, @Nonnull TextFlow tf) {
    tf.getChildren().add(new Text(expr.getReference()));
  }

  @Nonnull
  private Button editButton(@Nonnull IdeBean bean, @Nonnull Expr expr) {
    final Button button = new Button();
    button.setGraphic(BeanViewUtils.icon(expr));
    button.setOnAction(actionManager.editAction(bean, expr));
    return button;
  }

  @Nonnull
  private Button editInitializersButton(@Nonnull IdeBean bean, @Nonnull Expr expr) {
    final Button button = new Button();
    button.setGraphic(FontIcons.glyphIcon("D_VIEW_LIST"));
    button.setOnAction(actionManager.initializersEditAction(bean, expr));
    return button;
  }
}
