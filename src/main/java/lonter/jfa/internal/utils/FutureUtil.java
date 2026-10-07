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

package lonter.jfa.internal.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FutureUtil {
    @NotNull
    public static <T, U> CompletableFuture<U> thenApplyCancellable(
            @NotNull CompletableFuture<T> future, @NotNull Function<T, U> applyFunction, @Nullable Runnable onCancel) {
        CompletableFuture<U> cf = new CompletableFuture<>();

        future.thenAccept(t -> cf.complete(applyFunction.apply(t))).exceptionally(throwable -> {
            cf.completeExceptionally(throwable);
            return null;
        });

        cf.whenComplete((u, throwable) -> {
            if (cf.isCancelled()) {
                future.cancel(true);
                if (onCancel != null) {
                    onCancel.run();
                }
            }
        });

        return cf;
    }

    @NotNull
    public static <T, U> CompletableFuture<U> thenApplyCancellable(
            @NotNull CompletableFuture<T> future, @NotNull Function<T, U> applyFunction) {
        return thenApplyCancellable(future, applyFunction, null);
    }
}
