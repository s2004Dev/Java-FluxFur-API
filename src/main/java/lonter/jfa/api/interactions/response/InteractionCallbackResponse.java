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

package lonter.jfa.api.interactions.response;

import lonter.jfa.api.entities.Message;
import lonter.jfa.api.requests.restaction.interactions.InteractionCallbackAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interaction callback response that is created by interaction replies like
 * {@link lonter.jfa.api.interactions.callbacks.IReplyCallback#reply(String) IReplyCallback#reply()} or interaction updates like
 * {@link lonter.jfa.api.interactions.callbacks.IMessageEditCallback#editMessage(String) IMessageEditCallback#editMessage()}.
 *
 * @see lonter.jfa.api.interactions.InteractionHook#getCallbackResponse() InteractionHook#getCallbackResponse
 */
public interface InteractionCallbackResponse {
    /**
     * Returns the {@link Message} contained in this callback response, or {@code null} if none was created or updated by the response.
     *
     * @return Message contained in this callback response, or null.
     */
    @Nullable
    Message getMessage();

    /**
     * Returns the {@link InteractionCallbackAction.ResponseType ResponseType} that was used for replying to the interaction.
     *
     * @return ResponseType that was used for replying to the interaction.
     */
    @NotNull
    InteractionCallbackAction.ResponseType getResponseType();
}
