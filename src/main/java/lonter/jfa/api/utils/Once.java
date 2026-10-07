/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JFA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lonter.jfa.api.utils;

import lonter.jfa.api.JFA;
import lonter.jfa.api.events.GenericEvent;
import lonter.jfa.api.hooks.EventListener;
import lonter.jfa.api.hooks.SubscribeEvent;
import lonter.jfa.api.utils.concurrent.Task;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.JFALogger;
import lonter.jfa.internal.utils.concurrent.task.GatewayTask;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to listen to an event, once.
 *
 * @param <E> Type of the event listened to
 *
 * @see   JFA#listenOnce(Class)
 */
public class Once<E extends GenericEvent> implements EventListener {
    private static final Logger LOG = JFALogger.getLog(Once.class);

    private final JFA jfa;
    private final Class<E> eventType;
    private final List<Predicate<? super E>> filters;
    private final CompletableFuture<E> future;
    private final GatewayTask<E> task;
    private final ScheduledFuture<?> timeoutFuture;
    private final Runnable timeoutCallback;

    protected Once(
            JFA jfa,
            Class<E> eventType,
            List<Predicate<? super E>> filters,
            Runnable timeoutCallback,
            Duration timeout,
            ScheduledExecutorService timeoutPool) {
        this.jfa = jfa;
        this.eventType = eventType;
        this.filters = new ArrayList<>(filters);
        this.timeoutCallback = timeoutCallback;

        this.future = new CompletableFuture<>();
        this.task = createTask();
        this.timeoutFuture = scheduleTimeout(timeout, timeoutPool);
    }

    @NotNull
    private GatewayTask<E> createTask() {
        GatewayTask<E> task = new GatewayTask<>(future, () -> {
            // On cancellation, throw cancellation exception and cancel timeout
            jfa.removeEventListener(this);
            future.completeExceptionally(new CancellationException());
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
        });
        task.onSetTimeout(e -> {
            throw new UnsupportedOperationException("You must set the timeout on Once.Builder#timeout");
        });
        return task;
    }

    @Nullable
    private ScheduledFuture<?> scheduleTimeout(
            @Nullable Duration timeout, @Nullable ScheduledExecutorService timeoutPool) {
        if (timeout == null) {
            return null;
        }
        if (timeoutPool == null) {
            timeoutPool = jfa.getGatewayPool();
        }

        return timeoutPool.schedule(
                () -> {
                    // On timeout, throw timeout exception and run timeout callback
                    jfa.removeEventListener(this);
                    if (!future.completeExceptionally(new TimeoutException())) {
                        return;
                    }
                    if (timeoutCallback != null) {
                        try {
                            timeoutCallback.run();
                        } catch (Throwable e) {
                            LOG.error("An error occurred while running the timeout callback", e);
                            if (e instanceof Error) {
                                throw (Error) e;
                            }
                        }
                    }
                },
                timeout.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    @SubscribeEvent
    public void onEvent(@NotNull GenericEvent event) {
        if (!eventType.isInstance(event)) {
            return;
        }
        E casted = eventType.cast(event);
        try {
            if (filters.stream().allMatch(p -> p.test(casted))) {
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(false);
                }
                event.getJFA().removeEventListener(this);
                future.complete(casted);
            }
        } catch (Throwable e) {
            if (future.completeExceptionally(e)) {
                event.getJFA().removeEventListener(this);
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
        }
    }

    /**
     * Builds a one-time event listener, can be reused.
     *
     * @param <E> Type of the event listened to
     */
    public static class Builder<E extends GenericEvent> {
        private final JFA jfa;
        private final Class<E> eventType;
        private final List<Predicate<? super E>> filters = new ArrayList<>();

        private ScheduledExecutorService timeoutPool;
        private Duration timeout;
        private Runnable timeoutCallback;

        /**
         * Creates a builder for a one-time event listener
         *
         * @param jfa
         *        The JFA instance
         * @param eventType
         *        The event type to listen for
         *
         * @throws IllegalArgumentException
         *         If any of the parameters is null
         */
        public Builder(@NotNull JFA jfa, @NotNull Class<E> eventType) {
            Checks.notNull(jfa, "JFA");
            Checks.notNull(eventType, "Event type");
            this.jfa = jfa;
            this.eventType = eventType;
        }

        /**
         * Adds an event filter, all filters need to return {@code true} for the event to be consumed.
         *
         * <p>If the filter throws an exception, this listener will unregister itself.
         *
         * @param  filter
         *         The filter to add, returns {@code true} if the event can be consumed
         *
         * @throws IllegalArgumentException
         *         If the filter is null
         *
         * @return This instance for chaining convenience
         */
        @NotNull
        public Builder<E> filter(@NotNull Predicate<? super E> filter) {
            Checks.notNull(filter, "Filter");
            filters.add(filter);
            return this;
        }

        /**
         * Sets the timeout duration, after which the event is no longer listener for.
         *
         * @param  timeout
         *         The duration after which the event is no longer listener for
         *
         * @throws IllegalArgumentException
         *         If the timeout is null
         *
         * @return This instance for chaining convenience
         */
        @NotNull
        public Builder<E> timeout(@NotNull Duration timeout) {
            return timeout(timeout, null);
        }

        /**
         * Sets the timeout duration, after which the event is no longer listener for,
         * and the callback is run.
         *
         * @param  timeout
         *         The duration after which the event is no longer listener for
         * @param  timeoutCallback
         *         The callback run after the duration
         *
         * @throws IllegalArgumentException
         *         If the timeout is null
         *
         * @return This instance for chaining convenience
         */
        @NotNull
        public Builder<E> timeout(@NotNull Duration timeout, @Nullable Runnable timeoutCallback) {
            Checks.notNull(timeout, "Timeout");
            this.timeout = timeout;
            this.timeoutCallback = timeoutCallback;
            return this;
        }

        /**
         * Sets the thread pool used to schedule timeouts and run its callback.
         *
         * <p>By default {@link JFA#getGatewayPool()} is used.
         *
         * @param  timeoutPool
         *         The thread pool to use for timeouts
         *
         * @throws IllegalArgumentException
         *         If the timeout pool is null
         *
         * @return This instance for chaining convenience
         */
        @NotNull
        public Builder<E> setTimeoutPool(@NotNull ScheduledExecutorService timeoutPool) {
            Checks.notNull(timeoutPool, "Timeout pool");
            this.timeoutPool = timeoutPool;
            return this;
        }

        /**
         * Starts listening for the event, once.
         *
         * <p>The task will be completed after all {@link #filter(Predicate) filters} return {@code true}.
         *
         * <p>Exceptions thrown in {@link Task#get() blocking} and {@link Task#onSuccess(Consumer) async} contexts includes:
         * <ul>
         *     <li>{@link CancellationException} - When {@link Task#cancel()} is called</li>
         *     <li>{@link TimeoutException} - When the listener has expired</li>
         *     <li>Any exception thrown by the {@link #timeout(Duration, Runnable) timeout callback}</li>
         * </ul>
         *
         * @throws IllegalArgumentException
         *         If the callback is null
         *
         * @return {@link Task} returning an event satisfying all preconditions
         *
         * @see Task#onSuccess(Consumer)
         * @see Task#get()
         */
        @NotNull
        public Task<E> subscribe(@NotNull Consumer<E> callback) {
            Once<E> once = new Once<>(jfa, eventType, filters, timeoutCallback, timeout, timeoutPool);
            jfa.addEventListener(once);
            return once.task.onSuccess(callback);
        }
    }
}
