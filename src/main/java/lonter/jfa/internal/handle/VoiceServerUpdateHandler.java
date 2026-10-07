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

package lonter.jfa.internal.handle;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.hooks.VoiceDispatchInterceptor;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.audio.AudioConnection;
import lonter.jfa.internal.managers.AudioManagerImpl;
import lonter.jfa.internal.requests.WebSocketClient;

public class VoiceServerUpdateHandler extends SocketHandler {
    public VoiceServerUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }
        Guild guild = getJFA().getGuildById(guildId);
        if (guild == null) {
            throw new IllegalArgumentException("Attempted to start audio connection with Guild that doesn't exist!");
        }

        getJFA().getDirectAudioController()
                .update(guild, guild.getSelfMember().getVoiceState().getChannel());

        if (content.isNull("endpoint")) {
            // Fluxer did not provide an endpoint yet,
            // we are to wait until fluxer has resources to provide an endpoint,
            // which will result in them sending another VOICE_SERVER_UPDATE which we will handle
            // to actually connect to the audio server.
            return null;
        }

        String endpoint = content.getString("endpoint");
        String token = content.getString("token");
        String sessionId = guild.getSelfMember().getVoiceState().getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException(
                    "Attempted to create audio connection without having a session ID. Did VOICE_STATE_UPDATED fail?");
        }

        VoiceDispatchInterceptor voiceInterceptor = getJFA().getVoiceInterceptor();
        if (voiceInterceptor != null) {
            voiceInterceptor.onVoiceServerUpdate(
                    new VoiceDispatchInterceptor.VoiceServerUpdate(guild, endpoint, token, sessionId, allContent));
            return null;
        }

        AudioManagerImpl audioManager =
                (AudioManagerImpl) getJFA().getAudioManagersView().get(guildId);
        if (audioManager == null) {
            WebSocketClient.LOG.debug(
                    "Received a VOICE_SERVER_UPDATE but JFA is not currently connected nor attempted to connect "
                            + "to a VoiceChannel. Assuming that this is caused by another client running on this account. "
                            + "Ignoring the event.");
            return null;
        }

        MiscUtil.locked(audioManager.CONNECTION_LOCK, () -> {
            // Synchronized to prevent attempts to close while setting up initial objects.
            AudioChannel target = guild.getSelfMember().getVoiceState().getChannel();
            if (target == null) {
                WebSocketClient.LOG.warn("Ignoring VOICE_SERVER_UPDATE for unknown channel");
                return;
            }

            AudioConnection connection = new AudioConnection(audioManager, endpoint, sessionId, token, target);
            audioManager.setAudioConnection(connection);
            connection.startConnection();
        });
        return null;
    }
}
