/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.track;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class PeriodicObservable implements Observable, AutoCloseable {

	private final ConcurrentLinkedQueue<InvalidationListener> listeners = new ConcurrentLinkedQueue<>();
	private final ScheduledFuture<?> timerTask;

	public PeriodicObservable(ScheduledExecutorService executorService, long period, TimeUnit timeUnit) {
		this(task -> executorService.scheduleWithFixedDelay(task, period, period, timeUnit));
	}

	public PeriodicObservable(Function<Runnable, ScheduledFuture<?>> scheduler) {
		timerTask = scheduler.apply(() -> {
			if (!listeners.isEmpty()) {
				Platform.runLater(() -> listeners.forEach(l -> l.invalidated(this)));
			}
		});
	}

	@Override
	public void addListener(InvalidationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void close() {
		timerTask.cancel(false);
	}

	public BooleanBinding b(BooleanSupplier supplier, Observable... observables) {
		return Bindings.createBooleanBinding(supplier::getAsBoolean, observables(observables));
	}

	private Observable[] observables(Observable... observables) {
		final Observable[] result = Arrays.copyOf(observables, observables.length + 1);
		result[observables.length] = this;
		return result;
	}
}
