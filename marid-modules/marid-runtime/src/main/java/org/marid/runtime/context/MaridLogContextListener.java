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

package org.marid.runtime.context;

import org.marid.runtime.event.*;

import org.jetbrains.annotations.NotNull;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLogContextListener implements MaridContextListener {

  @Override
  public void bootstrap(@NotNull ContextBootstrapEvent contextBootstrapEvent) {
    log(INFO, "{0}", contextBootstrapEvent);
  }

  @Override
  public void onPostConstruct(@NotNull BeanPostConstructEvent postConstructEvent) {
    log(INFO, "{0}", postConstructEvent);
  }

  @Override
  public void onPreDestroy(@NotNull BeanPreDestroyEvent preDestroyEvent) {
    log(INFO, "{0}", preDestroyEvent);
  }

  @Override
  public void onStart(@NotNull ContextStartEvent contextStartEvent) {
    log(INFO, "{0}", contextStartEvent);
  }

  @Override
  public void onStop(@NotNull ContextStopEvent contextStopEvent) {
    log(INFO, "{0}", contextStopEvent);
  }

  @Override
  public void onFail(@NotNull ContextFailEvent contextFailEvent) {
    log(INFO, "{0}", contextFailEvent);
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}
