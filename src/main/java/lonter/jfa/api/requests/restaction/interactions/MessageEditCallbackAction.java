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

package lonter.jfa.api.requests.restaction.interactions;

import lonter.jfa.api.interactions.InteractionHook;
import lonter.jfa.api.requests.FluentRestAction;
import lonter.jfa.api.utils.messages.MessageEditRequest;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link InteractionCallbackAction} which can be used to edit the message for an interaction.
 */
public interface MessageEditCallbackAction
        extends InteractionCallbackAction<InteractionHook>,
                MessageEditRequest<MessageEditCallbackAction>,
                FluentRestAction<InteractionHook, MessageEditCallbackAction> {
    @NotNull
    @Override
    @CheckReturnValue
    MessageEditCallbackAction closeResources();
}
