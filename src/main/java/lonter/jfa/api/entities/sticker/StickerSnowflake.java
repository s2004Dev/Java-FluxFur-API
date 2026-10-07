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

package lonter.jfa.api.entities.sticker;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.internal.entities.sticker.StickerSnowflakeImpl;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract sticker reference by only the sticker ID.
 *
 * <p>This is used for methods which only need a sticker ID to function, you cannot use this for getting names or similar.
 * To get information about a sticker by their ID you can use {@link JFA#retrieveSticker(StickerSnowflake)} instead.
 */
public interface StickerSnowflake extends ISnowflake {
    /**
     * Creates a sticker snowflake instance which only wraps an ID.
     *
     * <p>This is primarily used for message sending purposes.
     *
     * @param  id
     *         The sticker id
     *
     * @return A sticker snowflake instance
     *
     * @see    JFA#retrieveSticker(StickerSnowflake)
     */
    @NotNull
    static StickerSnowflake fromId(long id) {
        return new StickerSnowflakeImpl(id);
    }

    /**
     * Creates a sticker snowflake instance which only wraps an ID.
     *
     * <p>This is primarily used for message sending purposes.
     *
     * @param  id
     *         The sticker id
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return A sticker snowflake instance
     *
     * @see    JFA#retrieveSticker(StickerSnowflake)
     */
    @NotNull
    static StickerSnowflake fromId(@NotNull String id) {
        return fromId(MiscUtil.parseSnowflake(id));
    }
}
