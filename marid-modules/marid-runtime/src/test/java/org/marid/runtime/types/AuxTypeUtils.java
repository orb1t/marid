/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.types;

import java.io.Closeable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public interface AuxTypeUtils {

  class List1<E extends Closeable> extends ArrayList<E> {

    public abstract class L<X extends E> implements List<X> {
    }

    public abstract class M implements List<E> {
    }
  }

  class List2 extends List1<Writer> {
  }

  class Map1<K extends I1<? super V>, V extends List<? extends K>> {

  }

  class Map2 extends Map1<I1<I1>, I1<I1>> {

  }

  interface I1<V> extends List<I1<V>> {

  }
}
