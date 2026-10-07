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
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;

import org.jetbrains.annotations.NotNull;

/**
 * This event is dispatched when a {@link ThreadChannel} that JFA didn't previously have access to (due to permissions) is now visible.
 *
 * <p>For example, if the bot is given the {@link Permission#ADMINISTRATOR} permission, any thread channels that the bot could not previously see would be "revealed".
 * This event is also used when the bot is added to a {@link ThreadChannel#isPublic() private} thread channel, revealing it through membership.
 *
 * @see ThreadHiddenEvent
 */
public class ThreadRevealedEvent extends GenericThreadEvent {
    public ThreadRevealedEvent(@NotNull JFA api, long responseNumber, ThreadChannel thread) {
        super(api, responseNumber, thread);
    }
}
