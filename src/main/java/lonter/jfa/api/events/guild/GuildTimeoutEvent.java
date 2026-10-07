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
import lonter.jfa.api.events.Event;
import lonter.jfa.api.events.session.ReadyEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a guild failed to ready up and timed out.
 * <br>Usually this event will be fired right before a {@link ReadyEvent ReadyEvent}.
 *
 * <p>This will mark the guild as <b>unavailable</b> and it will not be usable when JFA becomes ready.
 * You can check all unavailable guilds with {@link ReadyEvent#getGuildUnavailableCount()} and {@link JFA#getUnavailableGuilds()}.
 *
 * <p><b>Developer Note</b><br>
 *
 * <p>Fluxer may also explicitly mark guilds as unavailable during the setup, in which case this event will not fire.
 * It is recommended to check for unavailable guilds in the ready event explicitly to avoid any ambiguity.
 */
public class GuildTimeoutEvent extends Event {
    private final long guildId;

    public GuildTimeoutEvent(@NotNull JFA api, long guildId) {
        super(api);
        this.guildId = guildId;
    }

    /**
     * The guild id for the timed out guild
     *
     * @return The guild id
     */
    public long getGuildIdLong() {
        return guildId;
    }

    /**
     * The guild id for the timed out guild
     *
     * @return The guild id
     */
    @NotNull
    public String getGuildId() {
        return Long.toUnsignedString(guildId);
    }
}
