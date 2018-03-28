/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.app.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.marid.test.TestGroups.NORMAL;
import static org.testng.Assert.assertEquals;

public class HandlerPathTest {

  @DataProvider
  public Object[][] componentCountData() {
    return new Object[][] {
        {"", 0},
        {"/", 0},
        {"/a", 1},
        {"/a/b", 2},
        {"//b", 2}
    };
  }

  @Test(groups = {NORMAL}, dataProvider = "componentCountData")
  public void componentCount(String path, int expected) {
    final HandlerPath hp = new HandlerPath(path);
    final int actual = hp.getComponentCount();

    assertEquals(actual, expected);
  }

  @DataProvider
  public Object[][] subPathData() {
    return new Object[][] {
        {"/a/b/c", 1, "a"},
        {"/a/b/c", 2, "a/b"},
        {"/a/b/c", 3, "a/b/c"}
    };
  }

  @Test(groups = {NORMAL}, dataProvider = "subPathData")
  public void subPath(String path, int len, String expectedText) {
    final HandlerPath hp = new HandlerPath(path);
    final HandlerPath actual = hp.subPath(len);
    final HandlerPath expected = new HandlerPath(expectedText);

    assertEquals(actual, expected);
  }

  @Test(groups = {NORMAL}, expectedExceptions = IndexOutOfBoundsException.class)
  public void subPathIOOBE() {
    final HandlerPath hp = new HandlerPath("a/b");
    hp.subPath(3);
  }
}
