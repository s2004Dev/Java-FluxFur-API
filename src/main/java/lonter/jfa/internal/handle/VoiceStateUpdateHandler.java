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

import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.events.guild.voice.*;
import lonter.jfa.api.hooks.VoiceDispatchInterceptor;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.GuildVoiceStateImpl;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.managers.AudioManagerImpl;
import lonter.jfa.internal.requests.WebSocketClient;

import java.time.OffsetDateTime;
import java.util.Objects;

public class VoiceStateUpdateHandler extends SocketHandler {
    public VoiceStateUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        Long guildId = content.isNull("guild_id") ? null : content.getLong("guild_id");
        if (guildId == null) {
            return null; // unhandled for calls
        }
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        if (content.isNull("member")) {
            WebSocketClient.LOG.debug("Discarding VOICE_STATE_UPDATE with missing member. JSON: {}", content);
            return null;
        }

        handleGuildVoiceState(content);
        return null;
    }

    private void handleGuildVoiceState(DataObject content) {
        long userId = content.getLong("user_id");
        long guildId = content.getLong("guild_id");
        Long channelId = !content.isNull("channel_id") ? content.getLong("channel_id") : null;
        String sessionId = !content.isNull("session_id") ? content.getString("session_id") : null;
        boolean selfMuted = content.getBoolean("self_mute");
        boolean selfDeafened = content.getBoolean("self_deaf");
        boolean guildMuted = content.getBoolean("mute");
        boolean guildDeafened = content.getBoolean("deaf");
        boolean suppressed = content.getBoolean("suppress");
        boolean stream = content.getBoolean("self_stream");
        boolean video = content.getBoolean("self_video", false);
        String requestToSpeak = content.getString("request_to_speak_timestamp", null);
        OffsetDateTime requestToSpeakTime = null;
        long requestToSpeakTimestamp = 0L;
        if (requestToSpeak != null) {
            requestToSpeakTime = OffsetDateTime.parse(requestToSpeak);
            requestToSpeakTimestamp = requestToSpeakTime.toInstant().toEpochMilli();
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "Received a VOICE_STATE_UPDATE for a Guild that has yet to be cached. JSON: {}", content);
            return;
        }

        AudioChannel channel = null;
        if (channelId != null) {
            channel = (AudioChannel) guild.getGuildChannelById(channelId);
        }

        if (channel == null && (channelId != null)) {
            getJFA().getEventCache()
                    .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "Received VOICE_STATE_UPDATE for an AudioChannel that has yet to be cached. JSON: {}", content);
            return;
        }

        DataObject memberJson = content.getObject("member");
        MemberImpl member = getJFA().getEntityBuilder().createMember(guild, memberJson);

        GuildVoiceStateImpl vState = member.getVoiceState();
        if (vState == null) {
            if (guild.shouldCacheVoiceState(userId)) {
                vState = new GuildVoiceStateImpl(member);
            } else {
                return;
            }
        }

        vState.setSessionId(sessionId); // Cant really see a reason for an event for this
        VoiceDispatchInterceptor voiceInterceptor = getJFA().getVoiceInterceptor();
        boolean isSelf = guild.getSelfMember().equals(member);

        boolean wasMute = vState.isMuted();
        boolean wasDeaf = vState.isDeafened();

        if (selfMuted != vState.isSelfMuted()) {
            vState.setSelfMuted(selfMuted);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceSelfMuteEvent(getJFA(), responseNumber, member, selfMuted));
        }
        if (selfDeafened != vState.isSelfDeafened()) {
            vState.setSelfDeafened(selfDeafened);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceSelfDeafenEvent(getJFA(), responseNumber, member, selfDeafened));
        }
        if (guildMuted != vState.isGuildMuted()) {
            vState.setGuildMuted(guildMuted);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceGuildMuteEvent(getJFA(), responseNumber, member, guildMuted));
        }
        if (guildDeafened != vState.isGuildDeafened()) {
            vState.setGuildDeafened(guildDeafened);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceGuildDeafenEvent(getJFA(), responseNumber, member, guildDeafened));
        }
        if (suppressed != vState.isSuppressed()) {
            vState.setSuppressed(suppressed);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceSuppressEvent(getJFA(), responseNumber, member, suppressed));
        }
        if (stream != vState.isStream()) {
            vState.setStream(stream);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceStreamEvent(getJFA(), responseNumber, member, stream));
        }
        if (video != vState.isSendingVideo()) {
            vState.setVideo(video);
            getJFA().getEntityBuilder().updateMemberCache(member);
            getJFA().handleEvent(new GuildVoiceVideoEvent(getJFA(), responseNumber, member, video));
        }
        if (wasMute != vState.isMuted()) {
            getJFA().handleEvent(new GuildVoiceMuteEvent(getJFA(), responseNumber, member, vState.isMuted()));
        }
        if (wasDeaf != vState.isDeafened()) {
            getJFA().handleEvent(new GuildVoiceDeafenEvent(getJFA(), responseNumber, member, vState.isDeafened()));
        }
        if (requestToSpeakTimestamp != vState.getRequestToSpeak()) {
            OffsetDateTime oldRequestToSpeak = vState.getRequestToSpeakTimestamp();
            vState.setRequestToSpeak(requestToSpeakTime);
            getJFA().handleEvent(new GuildVoiceRequestToSpeakEvent(
                    getJFA(), responseNumber, member, oldRequestToSpeak, requestToSpeakTime));
        }

        if (!Objects.equals(channel, vState.getChannel())) {
            AudioChannel oldChannel = vState.getChannel();
            vState.updateConnectedChannel(channel);

            if (oldChannel == null) {
                getJFA().getEntityBuilder().updateMemberCache(member);
            } else if (channel == null) {
                if (isSelf) {
                    getJFA().getDirectAudioController().update(guild, null);
                }
                getJFA().getEntityBuilder().updateMemberCache(member, memberJson.isNull("joined_at"));
            } else {
                AudioManagerImpl mng =
                        (AudioManagerImpl) getJFA().getAudioManagersView().get(guildId);
                // If the currently connected account is the one that is being moved
                if (isSelf && mng != null && voiceInterceptor == null) {
                    // And this instance of JFA is connected or attempting to connect,
                    // then change the channel we expect to be connected to.
                    if (mng.isConnected()) {
                        mng.setConnectedChannel(channel);
                    }

                    // If we have connected (VOICE_SERVER_UPDATE received and AudioConnection
                    // created (actual connection might still be setting up)),
                    // then we need to stop sending audioOpen/Move requests through the MainWS
                    // if the channel we have just joined / moved to
                    // is the same as the currently queued audioRequest (handled by updateAudioConnection)
                    if (mng.isConnected()) {
                        getJFA().getDirectAudioController().update(guild, channel);
                    }
                    // If we are not already connected this will be removed by VOICE_SERVER_UPDATE
                }

                getJFA().getEntityBuilder().updateMemberCache(member);
            }

            getJFA().handleEvent(new GuildVoiceUpdateEvent(getJFA(), responseNumber, member, oldChannel));
        }

        if (isSelf && voiceInterceptor != null) {
            if (voiceInterceptor.onVoiceStateUpdate(
                    new VoiceDispatchInterceptor.VoiceStateUpdate(channel, vState, allContent))) {
                getJFA().getDirectAudioController().update(guild, channel);
            }
        }

        guild.updateRequestToSpeak();
    }
}
