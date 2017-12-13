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

package org.marid.dependant.beaneditor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.marid.idefx.expression.CallExpr;
import org.marid.idefx.expression.ClassExpr;
import org.marid.idefx.expression.Expr;
import org.marid.idefx.expression.NullExpr;
import org.marid.idefx.beans.IdeBean;
import org.marid.idelib.util.ClassTree;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActions;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.collections.MaridArrays.concat;
import static org.marid.jfx.action.SpecialActionType.ADD;

@Component
public class BeanActions {

  @Bean
  public BeanActionProvider addAction(SpecialActions specialActions, BeanDao beanDao) {
    return bean -> {
      final ObservableList<FxAction> actions = observableArrayList();

      final ClassTree classTree = new ClassTree(beanDao.publicClasses());

      // constructors
      final FxAction constructorsAction = constructors(bean, classTree);
      if (!constructorsAction.isEmpty()) {
        actions.add(constructorsAction);
      }

      // static methods
      final FxAction staticMethodsAction = staticMethods(bean, classTree);
      if (!staticMethodsAction.isEmpty()) {
        actions.add(staticMethodsAction);
      }

      return new FxAction(specialActions.get(ADD)).bindChildren(new SimpleObjectProperty<>(actions));
    };
  }

  @NotNull
  private FxAction constructors(@NotNull IdeBean bean, @NotNull ClassTree tree) {
    final FxAction action = new FxAction("constructor", "Actions");

    if (tree.name.isEmpty()) {
      action.bindText("Constructors");
    } else {
      action.setText(tree.name);
    }

    final FxAction[] pkgActions = tree.childStream()
        .map(c -> constructors(bean, c))
        .filter(a -> !a.isEmpty())
        .toArray(FxAction[]::new);
    final FxAction[] classActions = tree.classStream()
        .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
        .map(c -> {
          final FxAction a = new FxAction("class", "Actions").setText(c.getSimpleName());
          final FxAction[] constructors = of(c.getConstructors())
              .map(e -> {
                final Expr[] args = of(e.getParameters())
                    .map(p -> new NullExpr(p.getType().getTypeName()))
                    .toArray(NullExpr[]::new);
                final CallExpr expr = new CallExpr(new ClassExpr(c.getName()), "new", args);
                return new FxAction("constructor", "Actions")
                    .setText(of(e.getGenericParameterTypes()).map(Type::getTypeName).collect(joining(",", "(", ")")))
                    .setEventHandler(event -> {
                      final String name = c.getSimpleName();
                      bean.add(name, expr);
                    });
              })
              .toArray(FxAction[]::new);
          a.setChildren(concat(FxAction[]::new, constructors));
          return a;
        })
        .filter(e -> !e.isEmpty())
        .toArray(FxAction[]::new);

    action.setChildren(concat(FxAction[]::new, pkgActions, classActions));

    return action;
  }

  @NotNull
  private FxAction staticMethods(@NotNull IdeBean bean, @NotNull ClassTree tree) {
    final FxAction action = new FxAction("static-method", "Actions");

    if (tree.name.isEmpty()) {
      action.bindText("Static factories");
    } else {
      action.setText(tree.name);
    }

    final FxAction[] pkgActions = tree.childStream()
        .map(c -> staticMethods(bean, c))
        .filter(a -> !a.isEmpty())
        .toArray(FxAction[]::new);
    final FxAction[] classActions = tree.classStream()
        .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
        .map(c -> {
          final FxAction a = new FxAction("class", "Actions").setText(c.getSimpleName());
          final FxAction[] methods = of(c.getMethods())
              .filter(m -> Modifier.isStatic(m.getModifiers()))
              .filter(m -> !m.getReturnType().isPrimitive())
              .map(e -> {
                final Expr[] args = of(e.getParameters())
                    .map(p -> new NullExpr(p.getType().getTypeName()))
                    .toArray(NullExpr[]::new);
                final CallExpr expr = new CallExpr(new ClassExpr(c.getName()), e.getName(), args);
                return new FxAction("method", "Actions")
                    .setText(of(e.getGenericParameterTypes())
                        .map(Type::getTypeName)
                        .collect(joining(",", e.getName() + "(", ")")))
                    .setEventHandler(event -> {
                      final String name = c.getSimpleName();
                      bean.add(name, expr);
                    });
              })
              .toArray(FxAction[]::new);
          a.setChildren(concat(FxAction[]::new, methods));
          return a;
        })
        .filter(e -> !e.isEmpty())
        .toArray(FxAction[]::new);

    action.setChildren(concat(FxAction[]::new, pkgActions, classActions));

    return action;
  }
}
