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

package lonter.jfa.api.events.sticker;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.events.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that an {@link GuildSticker} was created/removed/updated.
 *
 * <p><b>Requirements</b>
 *
 * <p>These events require the {@link lonter.jfa.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericGuildStickerEvent extends Event {
    protected final Guild guild;
    protected final GuildSticker sticker;

    public GenericGuildStickerEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @NotNull GuildSticker sticker) {
        super(api, responseNumber);
        this.guild = guild;
        this.sticker = sticker;
    }

    /**
     * The relevant {@link GuildSticker} for this event
     *
     * @return The sticker
     */
    @NotNull
    public GuildSticker getSticker() {
        return sticker;
    }

    /**
     * The {@link Guild} this sticker belongs to
     *
     * @return The relevant guild
     */
    @NotNull
    public Guild getGuild() {
        return guild;
    }
}
