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

package it.lonter.jfa.api.entities.channel.concrete;

import it.lonter.jfa.api.JFA;
import it.lonter.jfa.api.entities.Guild;
import it.lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import it.lonter.jfa.api.entities.channel.middleman.StandardGuildMessageChannel;
import it.lonter.jfa.api.managers.channel.concrete.TextChannelManager;
import it.lonter.jfa.api.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Fluxer Text GuildChannel.
 * <br>Adds additional functionality and information for text channels in Fluxer,
 * on top of the common functionality present in other guild message channels.
 *
 * <p>This is a {@link it.lonter.jfa.api.entities.channel.middleman.GuildChannel GuildChannel} capable of sending messages.
 *
 * @see it.lonter.jfa.api.entities.channel.middleman.GuildChannel
 * @see it.lonter.jfa.api.entities.channel.middleman.MessageChannel
 * @see it.lonter.jfa.api.entities.channel.middleman.StandardGuildMessageChannel
 * @see   Guild#getTextChannelCache()
 * @see   Guild#getTextChannels()
 * @see   Guild#getTextChannelsByName(String, boolean)
 * @see   Guild#getTextChannelById(long)
 * @see   JFA#getTextChannelCache()
 * @see   JFA#getTextChannels()
 * @see   JFA#getTextChannelsByName(String, boolean)
 * @see   JFA#getTextChannelById(long)
 */
public interface TextChannel extends StandardGuildMessageChannel, ISlowmodeChannel {
    @NotNull
    @Override
    @CheckReturnValue
    ChannelAction<TextChannel> createCopy(@NotNull Guild guild);

    @NotNull
    @Override
    @CheckReturnValue
    default ChannelAction<TextChannel> createCopy() {
        return createCopy(getGuild());
    }

    @NotNull
    @Override
    @CheckReturnValue
    TextChannelManager getManager();
}
