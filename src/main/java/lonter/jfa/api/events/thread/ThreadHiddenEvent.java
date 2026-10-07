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

package lonter.jfa.api.events.thread;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;

import org.jetbrains.annotations.NotNull;

/**
 * This event is dispatched when a {@link ThreadChannel} that JFA did have access to is now inaccessible (due to permissions).
 * <br>
 * For example, if JFA can no longer access a TextChannel due to a change in permissions, this event will be dispatched for each child ThreadChannel of that channel.
 *
 * @see ThreadRevealedEvent
 */
public class ThreadHiddenEvent extends GenericThreadEvent {
    public ThreadHiddenEvent(@NotNull JFA api, long responseNumber, ThreadChannel thread) {
        super(api, responseNumber, thread);
    }
}
