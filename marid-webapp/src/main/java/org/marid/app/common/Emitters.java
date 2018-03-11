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

package org.marid.app.common;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Component
public class Emitters {

  private final ConcurrentLinkedQueue<SseEmitter> emitters = new ConcurrentLinkedQueue<>();
  private final Logger logger;

  public Emitters(Logger logger) {
    this.logger = logger;
  }

  public SseEmitter add() {
    final SseEmitter emitter = new SseEmitter(3_600_000L);
    emitter.onError(error -> logger.error("{} error", emitter, error));
    emitter.onTimeout(() -> emitter.completeWithError(new TimeoutException()));
    emitter.onCompletion(() -> {
      logger.info("{} completed", emitter);
      if (!emitters.remove(emitter)) {
        logger.warn("Unable to delete {}", emitter);
      }
    });
    logger.info("{} created", emitter);
    emitters.add(emitter);
    return emitter;
  }

  public void send(@NotNull Object value) {
    for (final SseEmitter emitter : emitters) {
      try {
        emitter.send(value, APPLICATION_JSON_UTF8);
      } catch (Exception x) {
        logger.error("Unable to send to {}", emitter, x);
      }
    }
  }
}
