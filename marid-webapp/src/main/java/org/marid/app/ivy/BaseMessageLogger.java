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

package org.marid.app.ivy;

import org.apache.ivy.util.MessageLogger;

import java.util.Collections;
import java.util.List;

import static org.apache.ivy.util.Message.*;

@FunctionalInterface
public interface BaseMessageLogger extends MessageLogger {

  @Override
  default void rawlog(String msg, int level) {
    log(msg, level);
  }

  @Override
  default void debug(String msg) {
    log(msg, MSG_DEBUG);
  }

  @Override
  default void info(String msg) {
    log(msg, MSG_INFO);
  }

  @Override
  default void rawinfo(String msg) {
    info(msg);
  }

  @Override
  default void error(String msg) {
    log(msg, MSG_ERR);
  }

  @Override
  default void verbose(String msg) {
    log(msg, MSG_VERBOSE);
  }

  @Override
  default void warn(String msg) {
    log(msg, MSG_WARN);
  }

  @Override
  default void deprecated(String msg) {
    log(msg, MSG_WARN);
  }

  @Override
  default List<String> getProblems() {
    return Collections.emptyList();
  }

  @Override
  default List<String> getWarns() {
    return Collections.emptyList();
  }

  @Override
  default List<String> getErrors() {
    return Collections.emptyList();
  }

  @Override
  default void clearProblems() {
  }

  @Override
  default void sumupProblems() {
  }

  @Override
  default void endProgress() {
  }

  @Override
  default void endProgress(String msg) {
  }

  @Override
  default boolean isShowProgress() {
    return false;
  }

  @Override
  default void setShowProgress(boolean progress) {
  }

  @Override
  default void progress() {
  }
}
