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

package lonter.jfa.api.interactions.callbacks;

import lonter.jfa.api.interactions.Interaction;
import lonter.jfa.api.interactions.InteractionHook;

import org.jetbrains.annotations.NotNull;

/**
 * Interactions which can be deferred.
 *
 * <p>This is implemented by {@link IReplyCallback} and {@link IMessageEditCallback}.
 */
public interface IDeferrableCallback extends Interaction {
    /**
     * The {@link InteractionHook} which can be used to send deferred replies or followup messages.
     *
     * @throws UnsupportedOperationException
     *         If this interaction does not support deferred replies and followup messages
     *
     * @return The interaction hook
     */
    @NotNull
    InteractionHook getHook();
}
