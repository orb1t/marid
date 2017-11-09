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
