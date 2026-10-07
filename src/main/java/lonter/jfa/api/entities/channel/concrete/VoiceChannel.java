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

package lonter.jfa.api.entities.channel.concrete;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.attribute.IAgeRestrictedChannel;
import lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import lonter.jfa.api.entities.channel.attribute.IVoiceStatusChannel;
import lonter.jfa.api.entities.channel.attribute.IWebhookContainer;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.GuildMessageChannel;
import lonter.jfa.api.entities.channel.middleman.StandardGuildChannel;
import lonter.jfa.api.managers.channel.concrete.VoiceChannelManager;
import lonter.jfa.api.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Fluxer Voice GuildChannel.
 * <br>Adds additional information specific to voice channels in Fluxer.
 *
 * @see GuildChannel
 * @see TextChannel
 * @see Category
 * @see   Guild#getVoiceChannelCache()
 * @see   Guild#getVoiceChannels()
 * @see   Guild#getVoiceChannelsByName(String, boolean)
 * @see   Guild#getVoiceChannelById(long)
 * @see   JFA#getVoiceChannelCache()
 * @see   JFA#getVoiceChannels()
 * @see   JFA#getVoiceChannelsByName(String, boolean)
 * @see   JFA#getVoiceChannelById(long)
 */
public interface VoiceChannel
        extends StandardGuildChannel,
                GuildMessageChannel,
                AudioChannel,
                IWebhookContainer,
                IAgeRestrictedChannel,
                ISlowmodeChannel,
                IVoiceStatusChannel {
    /**
     * The maximum limit you can set with {@link VoiceChannelManager#setUserLimit(int)}. ({@value})
     */
    int MAX_USERLIMIT = 99;

    @NotNull
    @Override
    @CheckReturnValue
    ChannelAction<VoiceChannel> createCopy(@NotNull Guild guild);

    @NotNull
    @Override
    @CheckReturnValue
    default ChannelAction<VoiceChannel> createCopy() {
        return createCopy(getGuild());
    }

    @NotNull
    @Override
    @CheckReturnValue
    VoiceChannelManager getManager();
}
