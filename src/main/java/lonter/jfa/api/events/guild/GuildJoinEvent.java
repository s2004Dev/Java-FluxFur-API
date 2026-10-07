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

package lonter.jfa.api.events.guild;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that you joined a {@link lonter.jfa.api.entities.Guild Guild}.
 * <br>This requires that the guild is available when the guild join happens. Otherwise a {@link UnavailableGuildJoinedEvent} is fired instead.
 *
 * <p><b>Warning: Fluxer already triggered a mass amount of these events due to a downtime. Be careful!</b>
 *
 * @see UnavailableGuildJoinedEvent
 */
public class GuildJoinEvent extends GenericGuildEvent {
    public GuildJoinEvent(@NotNull JFA api, long responseNumber, @NotNull Guild guild) {
        super(api, responseNumber, guild);
    }
}
