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
 * Indicates that a {@link lonter.jfa.api.entities.Guild Guild} became unavailable.
 * <br>Possibly due to a downtime or an outage. When it becomes available again a {@link GuildAvailableEvent} will be fired.
 *
 * <p>Can be used to detect that a Guild stopped responding.
 */
public class GuildUnavailableEvent extends GenericGuildEvent {
    public GuildUnavailableEvent(@NotNull JFA api, long responseNumber, @NotNull Guild guild) {
        super(api, responseNumber, guild);
    }
}
