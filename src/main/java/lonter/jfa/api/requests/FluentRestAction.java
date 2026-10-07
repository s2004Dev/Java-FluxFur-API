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

package lonter.jfa.api.requests;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used to mixin the customization parameters for {@link RestAction RestActions}.
 * <br>This simply fixes the return types to be the concrete implementation instead of the base interface.
 *
 * @param <T>
 *        The result type of the RestAction
 * @param <R>
 *        The concrete RestAction type used for chaining (fluent interface)
 */
@SuppressWarnings("unchecked")
public interface FluentRestAction<T, R extends FluentRestAction<T, R>> extends RestAction<T> {
    @NotNull
    @Override
    @CheckReturnValue
    R setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    default R addCheck(@NotNull BooleanSupplier checks) {
        return (R) RestAction.super.addCheck(checks);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default R timeout(long timeout, @NotNull TimeUnit unit) {
        return (R) RestAction.super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    @CheckReturnValue
    default R deadline(long timestamp) {
        return (R) RestAction.super.deadline(timestamp);
    }
}
