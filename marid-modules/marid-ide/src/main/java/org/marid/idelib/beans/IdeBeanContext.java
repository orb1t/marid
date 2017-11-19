/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.idelib.beans;

import org.marid.beans.BeanTypeContext;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IdeBeanContext extends BeanTypeContext {

  private final IdeBean bean;
  private final ClassLoader classLoader;
  private final List<Throwable> errors = new ArrayList<>();

  public IdeBeanContext(IdeBean bean, ClassLoader classLoader) {
    this.bean = bean;
    this.classLoader = classLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public <E extends Throwable> void throwError(@Nonnull E exception) throws E {
    errors.add(exception);
  }

  @Nonnull
  @Override
  public IdeBean getBean() {
    return bean;
  }

  @Nonnull
  @Override
  public Type getBeanType(@Nonnull String name) {
    return bean.matchingCandidates()
        .filter(b -> b.getName().equals(name))
        .findFirst()
        .map(b -> b.getFactory().getType(null, new IdeBeanContext((IdeBean) b, classLoader)))
        .orElseGet(() -> {
          errors.add(new IllegalStateException("Unable to get type of the bean " + name));
          return Object.class;
        });
  }
}
