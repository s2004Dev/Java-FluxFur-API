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

package lonter.jfa.api.utils.cache;

import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.utils.MiscUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized {@link SnowflakeCacheView} type used for handling channels.
 * <br>This type caches all relevant channel types, including threads.
 *
 * <p>Internally, this cache view makes a distinction between the varying {@link ChannelType ChannelTypes} and provides convenient methods to access a filtered subset.
 *
 * @param <T>
 *        The channel type
 */
public interface ChannelCacheView<T extends Channel> extends SnowflakeCacheView<T> {
    /**
     * Creates a decorator around this cache, filtered to only provide access to the given type.
     *
     * @param type
     *        The type class (Like {@code TextChannel.class})
     * @param <C>
     *        The type parameter
     *
     * @throws IllegalArgumentException
     *         If null is provided
     *
     * @return The filtered cache view
     */
    @NotNull
    <C extends T> ChannelCacheView<C> ofType(@NotNull Class<C> type);

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  type
     *         The expected {@link ChannelType}
     * @param  id
     *         The ID of the entity
     *
     * @return Possibly-null entity for the specified ID, null if the expected type is different from the actual type
     */
    @Nullable
    T getElementById(@NotNull ChannelType type, long id);

    /**
     * Retrieves the entity represented by the provided ID.
     *
     * @param  type
     *         The expected {@link ChannelType}
     * @param  id
     *         The ID of the entity
     *
     * @throws java.lang.NumberFormatException
     *         If the provided String is {@code null} or
     *         cannot be resolved to an unsigned long id
     *
     * @return Possibly-null entity for the specified ID, null if the expected type is different from the actual type
     */
    @Nullable
    default T getElementById(@NotNull ChannelType type, @NotNull String id) {
        return getElementById(type, MiscUtil.parseSnowflake(id));
    }
}
