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
import org.marid.beans.BeanTypeContext;
import org.marid.dependant.beaneditor.actions.BeanActionManager;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectProfile;
import org.marid.idefx.expression.*;
import org.marid.idefx.beans.IdeBean;
import org.marid.idefx.beans.IdeBeanContext;
import org.marid.runtime.MaridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Type;

import static org.marid.collections.MaridIterators.forEach;
import static org.marid.dependant.beaneditor.view.BeanViewUtils.replaceQualified;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

@Component
public class IdeBeanViewFactory {

  private final BeanActionManager actionManager;

  @Autowired
  public IdeBeanViewFactory(BeanActionManager actionManager) {
    this.actionManager = actionManager;
  }

  @NotNull
  public HBox createView(@NotNull IdeBean bean, @NotNull Expr expr) {
    final HBox tf = new HBox();
    tf.setAlignment(Pos.CENTER_LEFT);
    tf.setFillHeight(true);
    tf.getChildren().add(BeanViewUtils.icon(expr));
    tf.getChildren().add(new Label(" "));
    createView(bean, expr, tf);
    return tf;
  }

  @NotNull
  public Label beanLabel(@NotNull IdeBean bean) {
    final Label label = new Label();
    label.setText(bean.getName());
    label.setGraphic(bean.icon());
    label.setGraphicTextGap(6);
    return label;
  }

  @NotNull
  public Label typeLabel(@NotNull IdeBean bean, @NotNull ProjectProfile profile) {
    final BeanTypeContext typeContext = new IdeBeanContext(bean, profile.getClassLoader());
    final Type type = bean.getFactory().getType(null, typeContext);
    final String typeText = type.getTypeName();
    final String shortTypeText = replaceQualified(typeText);
    final Label label = new Label(shortTypeText, IdeShapes.rect(typeText.hashCode(), 16));
    label.setGraphicTextGap(6);
    return label;
  }

  private void createView(@NotNull IdeBean bean, @NotNull Expr expr, @NotNull HBox tf) {
    if (expr instanceof CallExpr) {
      createView(bean, (CallExpr) expr, tf);
    }else if (expr instanceof ArrayExpr) {
      createView(bean, (ArrayExpr) expr, tf);
    } else if (expr instanceof ClassExpr) {
      createView((ClassExpr) expr, tf);
    } else if (expr instanceof StringExpr) {
      createView((StringExpr) expr, tf);
    } else if (expr instanceof RefExpr) {
      createView((RefExpr) expr, tf);
    } else if (expr instanceof NullExpr) {
      createView((NullExpr) expr, tf);
    }
  }

  private void createView(@NotNull IdeBean bean, @NotNull CallExpr expr, @NotNull HBox tf) {
    if (!isShort(expr)) {
      createView(bean, expr.getTarget(), tf);
      tf.getChildren().add(new Label("."));
    }
    tf.getChildren().add(new Label(expr.getMethod() + "("));
    forEach(expr.getArgs(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Label(", "));
      }
      createView(bean, e, tf);
    });
    tf.getChildren().add(new Label(")"));
  }

  private void createView(@NotNull IdeBean bean, @NotNull ArrayExpr expr, @NotNull HBox tf) {
    tf.getChildren().add(new Label("["));
    forEach(expr.getElements(), (p, e) -> {
      if (p) {
        tf.getChildren().add(new Label(", "));
      }
      createView(bean, e, tf);
    });
    tf.getChildren().add(new Label("]"));
  }

  private void createView(@NotNull ClassExpr expr, @NotNull HBox tf) {
    final Label text = new Label(replaceQualified(expr.getClassName()));
    text.setTooltip(new Tooltip(expr.getClassName()));
    tf.getChildren().add(text);
  }

  private void createView(@NotNull StringExpr expr, @NotNull HBox tf) {
    tf.getChildren().add(new Label(expr.getValue()));
  }

  private void createView(@NotNull RefExpr expr, @NotNull HBox tf) {
    tf.getChildren().add(new Label(expr.getReference()));
  }

  private void createView(@NotNull NullExpr expr, @NotNull HBox tf) {
    tf.getChildren().add(new Label(replaceQualified(expr.getType()), glyphIcon("D_NULL")));
  }

  private static boolean isShort(@NotNull CallExpr expr) {
    final Expr t = expr.getTarget();
    return t instanceof ClassExpr && ((ClassExpr) t).getClassName().equals(MaridFactory.class.getName());
  }
}
