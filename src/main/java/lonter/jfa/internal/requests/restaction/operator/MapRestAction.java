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

package lonter.jfa.internal.requests.restaction.operator;

import lonter.jfa.api.exceptions.RateLimitedException;
import lonter.jfa.api.requests.RestAction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MapRestAction<I, O> extends RestActionOperator<I, O> {
    private final Function<? super I, ? extends O> function;

    public MapRestAction(RestAction<I> action, Function<? super I, ? extends O> function) {
        super(action);
        this.function = function;
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Consumer<? super Throwable> failure) {
        handle(action, failure, (result) -> doSuccess(success, function.apply(result)));
    }

    @Override
    public O complete(boolean shouldQueue) throws RateLimitedException {
        return function.apply(action.complete(shouldQueue));
    }

    @NotNull
    @Override
    public CompletableFuture<O> submit(boolean shouldQueue) {
        return action.submit(shouldQueue).thenApply(function);
    }
}
