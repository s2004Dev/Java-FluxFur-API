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
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.events.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Top-level thread event type
 * <br>All thread events JFA fires are derived from this class.
 *
 * <p>Can be used to check if an Object is a JFA event in {@link lonter.jfa.api.hooks.EventListener EventListener} implementations to distinguish what event is being fired.
 * <br>Adapter implementation: {@link lonter.jfa.api.hooks.ListenerAdapter ListenerAdapter}
 */
public class GenericThreadEvent extends Event {
    protected final ThreadChannel thread;

    public GenericThreadEvent(@NotNull JFA api, long responseNumber, ThreadChannel thread) {
        super(api, responseNumber);

        this.thread = thread;
    }

    /**
     * The event related {@link ThreadChannel} object
     *
     * @return The event related {@link ThreadChannel} object
     */
    @NotNull
    public ThreadChannel getThread() {
        return thread;
    }

    /**
     * The {@link Guild} containing the {@link ThreadChannel}.
     *
     * @return The {@link Guild} containing the {@link ThreadChannel}.
     */
    @NotNull
    public Guild getGuild() {
        return thread.getGuild();
    }
}
