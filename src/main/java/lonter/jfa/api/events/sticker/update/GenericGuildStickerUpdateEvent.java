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
import lonter.jfa.api.events.UpdateEvent;
import lonter.jfa.api.events.sticker.GenericGuildStickerEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that an {@link GuildSticker} was updated.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.utils.cache.CacheFlag#STICKER STICKER} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericGuildStickerUpdateEvent<T> extends GenericGuildStickerEvent
        implements UpdateEvent<GuildSticker, T> {
    protected final String identifier;
    protected final T previous;
    protected final T next;

    public GenericGuildStickerUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull Guild guild,
            @NotNull GuildSticker sticker,
            @NotNull String identifier,
            T oldValue,
            T newValue) {
        super(api, responseNumber, guild, sticker);
        this.identifier = identifier;
        this.previous = oldValue;
        this.next = newValue;
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return identifier;
    }

    @NotNull
    @Override
    public GuildSticker getEntity() {
        return getSticker();
    }

    @Nullable
    @Override
    public T getOldValue() {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue() {
        return next;
    }
}
