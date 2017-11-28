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

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.marid.dependant.beaneditor.actions.BeanActionManager;
import org.marid.expression.mutable.*;
import org.marid.idelib.beans.IdeBean;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class IdeBeanViewFactory {

  private final BeanActionManager actionManager;

  @Autowired
  public IdeBeanViewFactory(BeanActionManager actionManager) {
    this.actionManager = actionManager;
  }

  @Nonnull
  public Node createView(@Nonnull IdeBean bean, @Nonnull Expr expr) {
    if (bean.getParent() == null && bean.getFactory() == expr) {
      return FontIcons.glyphIcon("D_ROOMBA");
    } else {
      final Node node = createView(0, bean, expr);
      if (node instanceof HBox) {
        final HBox box = (HBox) node;
        box.getChildren().add(0, editButton(bean, expr));
        return box;
      } else {
        return new HBox(4, editButton(bean, expr), node);
      }
    }
  }

  @Nonnull
  private Node createView(int level, @Nonnull IdeBean bean, @Nonnull Expr expr) {
    if (level < 3) {
      if (expr instanceof ConstExpr) {
        return createView((ConstExpr) expr);
      } else if (expr instanceof CallExpr) {
        return createView(level, bean, (CallExpr) expr);
      } else if (expr instanceof GetExpr) {
        return createView(level, bean, (GetExpr) expr);
      } else if (expr instanceof SetExpr) {
        return createView(level, bean, (SetExpr) expr);
      } else if (expr instanceof ArrayExpr) {
        return createView(level, bean, (ArrayExpr) expr);
      } else if (expr instanceof ClassExpr) {
        return createView((ClassExpr) expr);
      } else if (expr instanceof StringExpr) {
        return createView((StringExpr) expr);
      } else if (expr instanceof RefExpr) {
        return createView((RefExpr) expr);
      }
    }
    return new HBox();
  }

  @Nonnull
  private Node createView(@Nonnull ConstExpr expr) {
    return new Text(expr.getValue());
  }

  @Nonnull
  private Node createView(int level, @Nonnull IdeBean bean, @Nonnull CallExpr expr) {
    final HBox box = new HBox(4);

    box.getChildren().add(editInitializersButton(bean, expr));
    box.getChildren().add(createView(level + 1, bean, expr.getTarget()));
    box.getChildren().add(new Text("." + expr.getMethod() + "("));

    expr.getArgs().stream().reduce((e1, e2) -> {
      box.getChildren().add(createView(level + 1, bean, e1));
      box.getChildren().add(new Text(","));
      return e2;
    }).ifPresent(e -> box.getChildren().add(createView(level + 1, bean, e)));

    box.getChildren().add(new Text(")"));
    return box;
  }

  @Nonnull
  private Node createView(int level, @Nonnull IdeBean bean, @Nonnull GetExpr expr) {
    final HBox box = new HBox(4);

    box.getChildren().add(editInitializersButton(bean, expr));
    box.getChildren().add(createView(level + 1, bean, expr.getTarget()));
    box.getChildren().add(new Text("." + expr.getField()));

    return box;
  }

  @Nonnull
  private Node createView(int level, @Nonnull IdeBean bean, @Nonnull SetExpr expr) {
    final HBox box = new HBox();

    box.getChildren().add(createView(level + 1, bean, expr.getTarget()));
    box.getChildren().add(new Text("." + expr.getField() + "="));
    box.getChildren().add(createView(level + 1, bean, expr.getValue()));

    return box;
  }

  @Nonnull
  private Node createView(int level, @Nonnull IdeBean bean, @Nonnull ArrayExpr expr) {
    final HBox box = new HBox(4);

    expr.getElements().stream().reduce((e1, e2) -> {
      box.getChildren().add(createView(level + 1, bean, e1));
      box.getChildren().add(new Text(","));
      return e2;
    }).ifPresent(e -> box.getChildren().add(createView(level + 1, bean, e)));

    return box;
  }

  @Nonnull
  private Node createView(@Nonnull ClassExpr expr) {
    final Label label = new Label(BeanViewUtils.replaceQualified(expr.getClassName()));
    label.setTooltip(new Tooltip(expr.getClassName()));
    return label;
  }

  @Nonnull
  private Node createView(@Nonnull StringExpr expr) {
    return new Text(expr.getValue());
  }

  @Nonnull
  private Node createView(@Nonnull RefExpr expr) {
    return new Text(expr.getReference());
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
