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
import lonter.jfa.api.entities.emoji.RichCustomEmoji;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link RichCustomEmoji Custom Emoji} was removed from a Guild.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI EMOJI} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 */
public class EmojiRemovedEvent extends GenericEmojiEvent {
    public EmojiRemovedEvent(@NotNull JFA api, long responseNumber, @NotNull RichCustomEmoji emoji) {
        super(api, responseNumber, emoji);
    }
}
