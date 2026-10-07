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

package lonter.jfa.api.audio.hooks;

import lonter.jfa.api.audio.SpeakingMode;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.UserSnowflake;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

/**
 * Used to monitor an audio connection, ping, and speaking users.
 * <br>This provides functionality similar to the functionalities present in the Fluxer client related to an audio connection.
 */
public interface ConnectionListener {
    /**
     * Called when JFA send a heartbeat packet to Fluxer and Fluxer sends an acknowledgement. The time difference
     * between sending and receiving the acknowledgement is calculated as the ping.
     *
     * @param  ping
     *         The time, in milliseconds, for round-trip packet travel to fluxer.
     */
    default void onPing(long ping) {}

    /**
     * Called when the status of the audio channel changes. Used to track the connection state of the audio connection
     * for easy debug and status display for clients.
     *
     * @param  status
     *         The new {@link lonter.jfa.api.audio.hooks.ConnectionStatus ConnectionStatus} of the audio connection.
     */
    default void onStatusChange(@NotNull ConnectionStatus status) {}

    /**
     * This method is used to listen for users changing their speaking mode.
     * <p>Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     * <p>To detect when a user is speaking, a {@link lonter.jfa.api.audio.AudioReceiveHandler AudioReceiveHandler} should be used instead.
     *
     * <p><b>Note:</b> This requires the user to be currently in the cache.
     * You can use {@link lonter.jfa.api.utils.MemberCachePolicy#VOICE MemberCachePolicy.VOICE} to cache currently connected users.
     * Alternatively, use {@link #onUserSpeakingModeUpdate(UserSnowflake, EnumSet)} to avoid cache.
     *
     * @param user
     *        The user who changed their speaking mode
     * @param modes
     *        The new speaking modes of the user
     */
    default void onUserSpeakingModeUpdate(@NotNull User user, @NotNull EnumSet<SpeakingMode> modes) {}

    /**
     * This method is used to listen for users changing their speaking mode.
     * <p>Whenever a user joins a voice channel, this is fired once to define the initial speaking modes.
     *
     * <p>To detect when a user is speaking, a {@link lonter.jfa.api.audio.AudioReceiveHandler AudioReceiveHandler} should be used instead.
     *
     * <p>This method works independently of the user cache. The provided user might not be cached.
     *
     * @param user
     *        The user who changed their speaking mode
     * @param modes
     *        The new speaking modes of the user
     */
    default void onUserSpeakingModeUpdate(@NotNull UserSnowflake user, @NotNull EnumSet<SpeakingMode> modes) {}
}
