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

package lonter.jfa.api.entities.channel.middleman;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Region;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.managers.channel.middleman.AudioChannelManager;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Guild Channel that is capable of handling audio.
 * <br>This is a {@link StandardGuildChannel} that contains additional methods present for audio channels
 *
 * @see VoiceChannel
 * @see StageChannel
 * @see Guild#getVoiceChannelCache()
 * @see Guild#getVoiceChannels()
 * @see Guild#getVoiceChannelsByName(String, boolean)
 * @see Guild#getVoiceChannelById(long)
 * @see Guild#getStageChannelCache()
 * @see Guild#getStageChannels()
 * @see Guild#getStageChannelsByName(String, boolean)
 * @see Guild#getStageChannelById(long)
 * @see JFA#getVoiceChannelById(long)
 * @see JFA#getStageChannelById(long)
 */
public interface AudioChannel extends StandardGuildChannel {
    @Override
    @NotNull
    @CheckReturnValue
    AudioChannelManager<?, ?> getManager();

    /**
     * The audio bitrate of the voice audio that is transmitted in this channel. While higher bitrates can be sent to
     * this channel, it will be scaled down by the client.
     * <br>Default and recommended value is 64000
     *
     * @return The audio bitrate of this audio channel.
     */
    int getBitrate();

    /**
     * The maximum amount of {@link lonter.jfa.api.entities.Member Members} that be in an audio connection within this channel concurrently.
     * <br>Returns 0 if there is no limit.
     *
     * <p>Moderators with the {@link lonter.jfa.api.Permission#VOICE_MOVE_OTHERS VOICE_MOVE_OTHERS} permission can bypass this limit.
     *
     * @return The maximum connections allowed in this channel concurrently
     */
    int getUserLimit();

    /**
     * The {@link Region} of this channel.
     * <br>This will return {@link Region#AUTOMATIC} if the region of this channel is set to Automatic.
     *
     * @return the {@link Region} of this channel.
     */
    @NotNull
    default Region getRegion() {
        return getRegionRaw() == null ? Region.AUTOMATIC : Region.fromKey(getRegionRaw());
    }

    /**
     * The raw region name for this channel.
     * <br>This will return null if the region is set to Automatic.
     *
     * @return Raw region name, or {@code null} if the region is set to automatic.
     */
    @Nullable
    String getRegionRaw();
}
