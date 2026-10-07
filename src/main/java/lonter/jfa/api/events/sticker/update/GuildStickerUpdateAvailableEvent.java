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

package lonter.jfa.api.events.sticker.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.sticker.GuildSticker;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the availability of a {@link GuildSticker} changed.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@code available}
 */
public class GuildStickerUpdateAvailableEvent extends GenericGuildStickerUpdateEvent<Boolean> {
    public static final String IDENTIFIER = "available";

    public GuildStickerUpdateAvailableEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            @NotNull GuildSticker sticker,
            boolean oldValue) {
        super(api, responseNumber, guild, sticker, IDENTIFIER, oldValue, sticker.isAvailable());
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Boolean getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Boolean getNewValue() {
        return super.getNewValue();
    }
}
