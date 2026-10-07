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

package lonter.jfa.api.managers.channel.attribute;

import lonter.jfa.api.entities.channel.attribute.IAgeRestrictedChannel;
import lonter.jfa.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Manager abstraction to set whether a channel is NSFW or Age-Restricted.
 *
 * @param <T> The channel type
 * @param <M> The manager type
 */
public interface IAgeRestrictedChannelManager<
                T extends IAgeRestrictedChannel, M extends IAgeRestrictedChannelManager<T, M>>
        extends ChannelManager<T, M> {
    /**
     * Sets the <b><u>nsfw flag</u></b> (also known as Age Restriction) of the selected {@link IAgeRestrictedChannel channel}.
     *
     * @param  nsfw
     *         The new nsfw flag for the selected {@link IAgeRestrictedChannel channel}.
     *
     * @return ChannelManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    M setNSFW(boolean nsfw);
}
