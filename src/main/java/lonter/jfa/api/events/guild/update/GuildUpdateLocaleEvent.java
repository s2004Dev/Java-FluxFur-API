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
import lonter.jfa.api.interactions.FluxerLocale;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link FluxerLocale} of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a Locale changes and retrieve the old one
 *
 * <p>Identifier: {@code locale}
 */
@SuppressWarnings("ConstantConditions")
public class GuildUpdateLocaleEvent extends GenericGuildUpdateEvent<FluxerLocale> {
    public static final String IDENTIFIER = "locale";

    public GuildUpdateLocaleEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @NotNull FluxerLocale previous) {
        super(api, responseNumber, guild, previous, guild.getLocale(), IDENTIFIER);
    }

    @NotNull
    @Override
    public FluxerLocale getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public FluxerLocale getNewValue() {
        return super.getNewValue();
    }
}
