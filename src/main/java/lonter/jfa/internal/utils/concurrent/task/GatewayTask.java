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

package lonter.jfa.internal.utils.concurrent.task;

import lonter.jfa.api.exceptions.ContextException;
import lonter.jfa.api.utils.concurrent.Task;
import lonter.jfa.internal.requests.WebSocketClient;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import org.jetbrains.annotations.NotNull;

public class GatewayTask<T> implements Task<T> {
    private static final Logger log = JFALogger.getLog(Task.class);
    private final Runnable onCancel;
    private final CompletableFuture<T> future;
    private LongConsumer setTimeout;

    public GatewayTask(CompletableFuture<T> future, Runnable onCancel) {
        this.future = future;
        this.onCancel = onCancel;
    }

    public GatewayTask<T> onSetTimeout(LongConsumer setTimeout) {
        this.setTimeout = setTimeout;
        return this;
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @NotNull
    @Override
    public Task<T> onError(@NotNull Consumer<? super Throwable> callback) {
        Checks.notNull(callback, "Callback");
        Consumer<Throwable> failureHandler =
                ContextException.here((error) -> log.error("Task Failure callback threw error", error));
        future.exceptionally(error -> {
            try {
                callback.accept(error);
            } catch (Throwable e) {
                failureHandler.accept(e);
                if (e instanceof Error) {
                    throw e;
                }
            }
            return null;
        });
        return this;
    }

    @NotNull
    @Override
    public Task<T> onSuccess(@NotNull Consumer<? super T> callback) {
        Checks.notNull(callback, "Callback");
        Consumer<Throwable> failureHandler =
                ContextException.here((error) -> log.error("Task Success callback threw error", error));
        future.thenAccept(result -> {
            try {
                callback.accept(result);
            } catch (Throwable error) {
                failureHandler.accept(error);
                if (error instanceof Error) {
                    throw error;
                }
            }
        });
        return this;
    }

    @NotNull
    @Override
    public Task<T> setTimeout(@NotNull Duration timeout) {
        Checks.notNull(timeout, "Timeout");
        long millis = timeout.toMillis();
        Checks.positive(millis, "Timeout");
        if (this.setTimeout != null) {
            this.setTimeout.accept(millis);
        }
        return this;
    }

    @NotNull
    @Override
    public T get() {
        if (WebSocketClient.WS_THREAD.get()) {
            throw new UnsupportedOperationException("Blocking operations are not permitted on the gateway thread");
        }
        return future.join();
    }

    @Override
    public void cancel() {
        onCancel.run();
    }
}
