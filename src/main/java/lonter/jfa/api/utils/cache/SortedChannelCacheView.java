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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;

import org.jetbrains.annotations.NotNull;

/**
 * Specialized {@link ChannelCacheView} type used for handling sorted lists of channels.
 * <br>Sorting is done with respect to the positioning in the official Fluxer client, by comparing positions and category information.
 *
 * <p>Internally, this cache view makes a distinction between the varying {@link ChannelType ChannelTypes} and provides convenient methods to access a filtered subset.
 *
 * @param <T>
 *        The channel type
 *
 * @see   Guild#getChannels()
 */
public interface SortedChannelCacheView<T extends Channel & Comparable<? super T>>
        extends ChannelCacheView<T>, SortedSnowflakeCacheView<T> {
    @NotNull
    <C extends T> SortedChannelCacheView<C> ofType(@NotNull Class<C> type);
}
