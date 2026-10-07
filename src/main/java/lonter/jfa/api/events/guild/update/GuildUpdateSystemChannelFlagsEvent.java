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

package lonter.jfa.api.events.guild.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.guild.SystemChannelFlag;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that {@link Guild#getSystemChannelFlags()} have been updated.
 *
 * <p>This event can be used to detect changes in system channel flags and retrieve the old ones.
 *
 * <p>Identifier: {@code system_channel_flags}
 */
public class GuildUpdateSystemChannelFlagsEvent extends GenericGuildUpdateEvent<Set<SystemChannelFlag>> {
    public static final String IDENTIFIER = "system_channel_flags";

    public GuildUpdateSystemChannelFlagsEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            @NotNull Set<SystemChannelFlag> oldFlags,
            @NotNull Set<SystemChannelFlag> newFlags) {
        super(api, responseNumber, guild, oldFlags, newFlags, IDENTIFIER);
    }

    /**
     * Returns the old system channel flags for this guild.
     *
     * @return An unmodifiable set of old system channel flags for this guild.
     */
    @NotNull
    public Set<SystemChannelFlag> getOldFlags() {
        return getOldValue();
    }

    /**
     * Returns the new system channel flags for this guild.
     *
     * @return An unmodifiable set of new system channel flags for this guild.
     */
    @NotNull
    public Set<SystemChannelFlag> getNewFlags() {
        return getNewValue();
    }
}
