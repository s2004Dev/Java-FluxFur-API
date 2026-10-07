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

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link lonter.jfa.api.entities.Guild.Timeout AFK-Timeout} of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when an afk timeout changes and retrieve the old one
 *
 * <p>Identifier: {@code afk_timeout}
 */
public class GuildUpdateAfkTimeoutEvent extends GenericGuildUpdateEvent<Guild.Timeout> {
    public static final String IDENTIFIER = "afk_timeout";

    public GuildUpdateAfkTimeoutEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @NotNull Guild.Timeout oldAfkTimeout) {
        super(api, responseNumber, guild, oldAfkTimeout, guild.getAfkTimeout(), IDENTIFIER);
    }

    /**
     * The old {@link lonter.jfa.api.entities.Guild.Timeout AFK-Timeout}
     *
     * @return The old AFK-Timeout
     */
    @NotNull
    public Guild.Timeout getOldAfkTimeout() {
        return getOldValue();
    }

    /**
     * The new {@link lonter.jfa.api.entities.Guild.Timeout AFK-Timeout}
     *
     * @return The new AFK-Timeout
     */
    @NotNull
    public Guild.Timeout getNewAfkTimeout() {
        return getNewValue();
    }

    @NotNull
    @Override
    public Guild.Timeout getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public Guild.Timeout getNewValue() {
        return super.getNewValue();
    }
}
