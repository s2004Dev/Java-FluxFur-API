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

package lonter.jfa.api.events.emoji.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the name of a {@link RichCustomEmoji Custom Emoji} changed.
 *
 * <p>Can be used to retrieve the old name
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.utils.cache.CacheFlag#EMOJI EMOJI} CacheFlag to be enabled, which requires
 * the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_EXPRESSIONS GUILD_EXPRESSIONS} intent.
 *
 * <br>{@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disables that CacheFlag by default!
 *
 * <p>Identifier: {@code name}
 */
public class EmojiUpdateNameEvent extends GenericEmojiUpdateEvent<String> {
    public static final String IDENTIFIER = "name";

    public EmojiUpdateNameEvent(
            @NotNull JFA api, long responseNumber, @NotNull RichCustomEmoji emoji, @NotNull String oldName) {
        super(api, responseNumber, emoji, oldName, emoji.getName(), IDENTIFIER);
    }

    /**
     * The old name
     *
     * @return The old name
     */
    @NotNull
    public String getOldName() {
        return getOldValue();
    }

    /**
     * The new name
     *
     * @return The new name
     */
    @NotNull
    public String getNewName() {
        return getNewValue();
    }

    @NotNull
    @Override
    public String getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public String getNewValue() {
        return super.getNewValue();
    }
}
