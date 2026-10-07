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

package lonter.jfa.api.events.emoji;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.events.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link RichCustomEmoji Custom Emoji} was created/removed/updated.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI EMOJI} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public abstract class GenericEmojiEvent extends Event {
    protected final RichCustomEmoji emoji;

    public GenericEmojiEvent(@NotNull JFA api, long responseNumber, @NotNull RichCustomEmoji emoji) {
        super(api, responseNumber);
        this.emoji = emoji;
    }

    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} where the emoji came from
     *
     * @return The origin Guild
     */
    @NotNull
    public Guild getGuild() {
        return emoji.getGuild();
    }

    /**
     * The affected {@link RichCustomEmoji} for this event
     *
     * @return The emoji
     */
    @NotNull
    public RichCustomEmoji getEmoji() {
        return emoji;
    }

    /**
     * Whether this emoji is managed by an integration
     *
     * @return True, if this emoji is managed by an integration
     */
    public boolean isManaged() {
        return emoji.isManaged();
    }
}
