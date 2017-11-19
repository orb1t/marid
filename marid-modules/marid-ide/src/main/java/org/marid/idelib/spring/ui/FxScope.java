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

package org.marid.idelib.spring.ui;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

public class FxScope implements Scope {

  private final Set<String> conversationIds = new ConcurrentSkipListSet<>();
  private final ConcurrentMap<String, ConcurrentMap<String, Object>> map = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, ConcurrentMap<String, Runnable>> destroyers = new ConcurrentHashMap<>();

  private volatile String conversationId;

  String nextConversationId() {
    return IntStream.range(0, Integer.MAX_VALUE)
        .mapToObj(Integer::toString)
        .filter(v -> !conversationIds.contains(v))
        .findFirst()
        .orElse("-");
  }

  void setConversationId(String conversationId) {
    conversationIds.add(conversationId);
    if (!conversationId.equals(this.conversationId)) {
      this.conversationId = conversationId;
      log(INFO, "Activated FxScope[{0}]", conversationId);
    }
  }

  void destroy(String conversationId) {
    conversationIds.remove(conversationId);
    map.remove(conversationId);
    ofNullable(destroyers.remove(conversationId)).ifPresent(d -> d.forEach((k, v) -> {
      try {
        v.run();
      } catch (Throwable x) {
        log(WARNING, "Unable to destroy FxScope[{0}].{1}", x, conversationId, k);
      }
    }));
    log(INFO, "Destroyed FxScope[{0}]", conversationId);
  }

  @Nonnull
  @Override
  public Object get(@Nonnull String name, @Nonnull ObjectFactory<?> objectFactory) {
    return map
        .computeIfAbsent(conversationId, k -> new ConcurrentHashMap<>())
        .computeIfAbsent(name, k -> objectFactory.getObject());
  }

  @Nullable
  @Override
  public Object remove(@Nonnull String name) {
    return Stream.ofNullable(map.remove(conversationId))
        .map(m -> m.get(name))
        .findFirst()
        .orElse(null);
  }

  @Override
  public void registerDestructionCallback(@Nonnull String name, @Nonnull Runnable callback) {
    destroyers
        .computeIfAbsent(conversationId, k -> new ConcurrentHashMap<>())
        .put(name, callback);
  }

  @Nullable
  @Override
  public Object resolveContextualObject(@Nonnull String key) {
    return null;
  }

  @Nullable
  @Override
  public String getConversationId() {
    return conversationId;
  }
}
