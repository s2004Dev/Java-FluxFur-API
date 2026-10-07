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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.requests.FluentRestAction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraction for requests related to webhook executions.
 * <br>This adds the ability to change the context in which the webhook is used, by setting a {@link #setThread(ThreadChannel) thread id}.
 *
 * @param <T>
 *        The result type
 * @param <R>
 *        The action type
 */
public interface AbstractWebhookMessageAction<T, R extends AbstractWebhookMessageAction<T, R>>
        extends FluentRestAction<T, R> {
    /**
     * Set the target thread id for the webhook message.
     * <br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     * <p>This cannot be used with {@link lonter.jfa.api.interactions.InteractionHook InteractionHooks}!
     *
     * @param  threadId
     *         The target thread id or null to unset
     *
     * @throws IllegalStateException
     *         If this is an interaction webhook
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return The same message action, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    R setThreadId(@Nullable String threadId);

    /**
     * Set the target thread id for the webhook message.
     * <br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     * <p>This cannot be used with {@link lonter.jfa.api.interactions.InteractionHook InteractionHooks}!
     *
     * @param  threadId
     *         The target thread id or 0 to unset
     *
     * @throws IllegalStateException
     *         If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default R setThreadId(long threadId) {
        return setThreadId(threadId == 0 ? null : Long.toUnsignedString(threadId));
    }

    /**
     * Set the target thread for the webhook message.
     * <br>This allows sending webhook messages in the target thread,
     * however the webhook must be part of the thread parent channel.
     *
     * <p>This cannot be used with {@link lonter.jfa.api.interactions.InteractionHook InteractionHooks}!
     *
     * @param  channel
     *         The target thread channel
     *
     * @throws IllegalStateException
     *         If this is an interaction webhook
     *
     * @return The same message action, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default R setThread(@Nullable ThreadChannel channel) {
        return setThreadId(channel == null ? null : channel.getId());
    }
}
