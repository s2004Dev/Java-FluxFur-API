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

package lonter.jfa.api.audio.dave;

import lonter.jfa.api.JFABuilder;
import lonter.jfa.api.audio.AudioModuleConfig;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.managers.AudioManager;
import lonter.jfa.api.sharding.DefaultShardManagerBuilder;

import org.jetbrains.annotations.NotNull;

/**
 * Factory for {@link DaveSession DaveSessions}.
 *
 * <p>All audio/video connections on Fluxer use the "Fluxer Audio &amp; Video Encryption" (DAVE) protocol.
 * By default, JFA does not implement this and falls back to "passthrough mode", which only does transport encryption through AEAD.
 *
 * <p>To use audio connections with JFA (such as {@link AudioManager#openAudioConnection(AudioChannel)}),
 * you must provide an implementation of this factory
 * either in {@link JFABuilder#setAudioModuleConfig(AudioModuleConfig)} or {@link DefaultShardManagerBuilder#setAudioModuleConfig(AudioModuleConfig)}.
 */
public interface DaveSessionFactory {
    /**
     * Create a new DAVE session.
     *
     * <p>The session should not yet be started.
     * JFA will invoke {@link DaveSession#initialize()} once a connection to the voice gateway is established.
     *
     * <p>The dave session should be started/created in {@link DaveSession#onSelectProtocolAck(int)},
     * which will provide the target protocol version.
     *
     * @param callbacks
     *        The {@link DaveProtocolCallbacks callbacks} to facilitate the protocol communication
     * @param userId
     *        The id of the connecting bot (self user)
     * @param channelId
     *        The id of the channel or group of the connection (audio channel)
     *
     * @return {@link DaveSession}
     */
    @NotNull
    DaveSession createDaveSession(@NotNull DaveProtocolCallbacks callbacks, long userId, long channelId);
}
