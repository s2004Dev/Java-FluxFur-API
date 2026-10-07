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
 * Indicates that the {@link lonter.jfa.api.entities.Guild.VerificationLevel VerificationLevel} of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a VerificationLevel changes and retrieve the old one
 *
 * <p>Identifier: {@code verification_level}
 */
public class GuildUpdateVerificationLevelEvent extends GenericGuildUpdateEvent<Guild.VerificationLevel> {
    public static final String IDENTIFIER = "verification_level";

    public GuildUpdateVerificationLevelEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            @NotNull Guild.VerificationLevel oldVerificationLevel) {
        super(api, responseNumber, guild, oldVerificationLevel, guild.getVerificationLevel(), IDENTIFIER);
    }

    /**
     * The old {@link lonter.jfa.api.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The old VerificationLevel
     */
    @NotNull
    public Guild.VerificationLevel getOldVerificationLevel() {
        return getOldValue();
    }

    /**
     * The new {@link lonter.jfa.api.entities.Guild.VerificationLevel VerificationLevel}
     *
     * @return The new VerificationLevel
     */
    @NotNull
    public Guild.VerificationLevel getNewVerificationLevel() {
        return getNewValue();
    }

    @NotNull
    @Override
    public Guild.VerificationLevel getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public Guild.VerificationLevel getNewValue() {
        return super.getNewValue();
    }
}
