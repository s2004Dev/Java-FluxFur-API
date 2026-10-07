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

package lonter.jfa.api.managers;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;

import org.jetbrains.annotations.NotNull;

/**
 * Direct access to internal gateway communication.
 * <br>This should only be used if a {@link lonter.jfa.api.hooks.VoiceDispatchInterceptor VoiceDispatchInterceptor} has been provided.
 *
 * <p>For normal operation use {@link Guild#getAudioManager()} instead.
 */
public interface DirectAudioController {
    /**
     * The associated JFA instance
     *
     * @return The JFA instance
     */
    @NotNull
    JFA getJFA();

    /**
     * Requests a voice server endpoint for connecting to the voice gateway.
     *
     * @param channel
     *        The channel to connect to
     *
     * @see   #reconnect(AudioChannel)
     */
    void connect(@NotNull AudioChannel channel);

    /**
     * Requests to terminate the connection to a voice channel.
     *
     * @param guild
     *        The guild we were connected to
     *
     * @see   #reconnect(AudioChannel)
     */
    void disconnect(@NotNull Guild guild);

    /**
     * Requests to reconnect to the voice channel in the target guild.
     *
     * @param channel
     *        The channel we were connected to
     */
    void reconnect(@NotNull AudioChannel channel);
}
