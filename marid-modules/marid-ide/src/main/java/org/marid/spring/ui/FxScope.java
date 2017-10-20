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

package org.marid.spring.ui;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static java.lang.Integer.toUnsignedString;
import static java.lang.System.identityHashCode;
import static java.util.Optional.ofNullable;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

public class FxScope implements Scope {

    private volatile String conversationId;
    private final ConcurrentMap<String, ConcurrentMap<String, Object>> map = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, Runnable>> destroyers = new ConcurrentHashMap<>();

    public void setConversationId(String conversationId) {
        if (!conversationId.equals(this.conversationId)) {
            this.conversationId = conversationId;
            log(INFO, "Activated {0}", conversationId);
        }
    }

    public void destroy(String conversationId) {
        map.remove(conversationId);
        ofNullable(destroyers.remove(conversationId)).ifPresent(d -> d.forEach((k, v) -> {
            try {
                v.run();
                log(INFO, "Destroyed {0}.{1}", conversationId, k);
            } catch (Throwable x) {
                log(WARNING, "Unable to destroy {0}.{1}", x, conversationId, k);
            }
        }));
        log(INFO, "Destroyed {0}", conversationId);
    }

    @Nonnull
    @Override
    public Object get(@Nonnull String name, @Nonnull ObjectFactory<?> objectFactory) {
        log(INFO, "Get {0}.{1}", conversationId, name);
        final Object result = map
                .computeIfAbsent(conversationId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, k -> objectFactory.getObject());
        log(INFO, "Got {0}.{1} {2}", conversationId, name, pp(result));
        return result;
    }

    @Nullable
    @Override
    public Object remove(@Nonnull String name) {
        return Stream.ofNullable(map.remove(conversationId))
                .map(m -> m.get(name))
                .peek(v -> log(INFO, "Removed {0}.{1} {2}", conversationId, name, pp(v)))
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

    private String pp(Object v) {
        return toUnsignedString(identityHashCode(v), 16);
    }
}
