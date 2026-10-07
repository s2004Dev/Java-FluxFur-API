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

package lonter.jfa.api.events;

import lonter.jfa.api.JFA;
import lonter.jfa.api.utils.data.DataObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for events supported by {@link lonter.jfa.api.hooks.IEventManager EventManagers}.
 *
 * @see lonter.jfa.api.hooks.EventListener#onEvent(GenericEvent)
 */
public interface GenericEvent {
    /**
     * The current JFA instance corresponding to this Event
     *
     * @return The corresponding JFA instance
     */
    @NotNull
    JFA getJFA();

    /**
     * The current sequence for this event.
     * <br>This can be used to keep events in order when making sequencing system.
     *
     * @return The current sequence number for this event
     */
    long getResponseNumber();

    /**
     * The passthrough data that this event was serialized from. This data might be null in rare situations, for example, if the event came from a rest action.
     * <br>This provides the full gateway message payload, including sequence, event name and dispatch type.
     * For details, read the official <a href="https://fluxer.dev/topics/gateway" target="_blank">Fluxer Documentation</a>.
     *
     * @throws IllegalStateException
     *         If event passthrough was not enabled, see {@link lonter.jfa.api.JFABuilder#setEventPassthrough(boolean) JFABuilder#setEventPassthrough(boolean)}
     *
     * @return The corresponding {@link DataObject}
     */
    @Nullable
    DataObject getRawData();
}
