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
import lonter.jfa.api.entities.channel.concrete.TextChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the system channel of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 * <br>This is used for welcome messages
 *
 * <p>Can be used to detect when a guild system channel changes and retrieve the old one
 *
 * <p>Identifier: {@code system_channel}
 */
public class GuildUpdateSystemChannelEvent extends GenericGuildUpdateEvent<TextChannel> {
    public static final String IDENTIFIER = "system_channel";

    public GuildUpdateSystemChannelEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @Nullable TextChannel oldSystemChannel) {
        super(api, responseNumber, guild, oldSystemChannel, guild.getSystemChannel(), IDENTIFIER);
    }

    /**
     * The previous system channel.
     *
     * @return The previous system channel
     */
    @Nullable
    public TextChannel getOldSystemChannel() {
        return getOldValue();
    }

    /**
     * The new system channel.
     *
     * @return The new system channel
     */
    @Nullable
    public TextChannel getNewSystemChannel() {
        return getNewValue();
    }
}
