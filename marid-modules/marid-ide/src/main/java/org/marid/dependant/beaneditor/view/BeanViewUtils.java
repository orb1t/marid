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

import javafx.scene.text.Text;
import org.marid.expression.mutable.*;
import org.marid.jfx.icons.FontIcons;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class BeanViewUtils {

  private static final Pattern QUALIFIED_NAME = compile("((\\w+)[.])*(?<simpleName>\\w+)");

  @Nonnull
  public static String replaceQualified(@Nonnull String text) {
    final Matcher matcher = QUALIFIED_NAME.matcher(text);
    return matcher.replaceAll(r -> ((Matcher) r).group("simpleName"));
  }

  @Nonnull
  public static Text icon(@Nonnull Expr expr) {
    if (expr instanceof CallExpr) {
      return FontIcons.glyphIcon("F_CODE");
    } else if (expr instanceof GetExpr) {
      return FontIcons.glyphIcon("F_GET_POCKET");
    } else if (expr instanceof SetExpr) {
      return FontIcons.glyphIcon("D_ARROW_RIGHT");
    } else if (expr instanceof ThisExpr) {
      return FontIcons.glyphIcon("D_THERMOMETER");
    } else if (expr instanceof NullExpr) {
      return FontIcons.glyphIcon("D_NULL");
    } else if (expr instanceof RefExpr) {
      return FontIcons.glyphIcon("F_LINK");
    } else if (expr instanceof StringExpr) {
      return FontIcons.glyphIcon("D_CODE_STRING");
    } else if (expr instanceof ClassExpr) {
      return FontIcons.glyphIcon("D_ADJUST");
    } else if (expr instanceof ArrayExpr) {
      return FontIcons.glyphIcon("D_CODE_ARRAY");
    } else {
      return FontIcons.glyphIcon("M_DEVICES_OTHER");
    }
  }
}
