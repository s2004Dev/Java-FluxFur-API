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
 * Indicates that the safety alert channel of a {@link Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild safety alert changes and retrieve the old one
 *
 * <p>Identifier: {@code safety_alerts_channel}
 */
public class GuildUpdateSafetyAlertsChannelEvent extends GenericGuildUpdateEvent<TextChannel> {
    public static final String IDENTIFIER = "safety_alerts_channel";

    public GuildUpdateSafetyAlertsChannelEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @Nullable TextChannel oldSafetyAlertsChannel) {
        super(api, responseNumber, guild, oldSafetyAlertsChannel, guild.getSafetyAlertsChannel(), IDENTIFIER);
    }

    /**
     * The previous safety alert channel.
     *
     * @return The previous safety alert channel
     */
    @Nullable
    public TextChannel getOldSafetyAlertsChannel() {
        return getOldValue();
    }

    /**
     * The new safety alert channel.
     *
     * @return The new safety alert channel
     */
    @Nullable
    public TextChannel getNewSafetyAlertsChannel() {
        return getNewValue();
    }
}
