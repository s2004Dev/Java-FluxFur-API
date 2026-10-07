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

package lonter.jfa.internal.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.GuildVoiceState;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.entities.channel.unions.AudioChannelUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.requests.CompletedRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

public class GuildVoiceStateImpl implements GuildVoiceState {
    private final JFA api;
    private Guild guild;
    private Member member;

    private AudioChannel connectedChannel;
    private String sessionId;
    private long requestToSpeak;
    private boolean selfMuted = false;
    private boolean selfDeafened = false;
    private boolean guildMuted = false;
    private boolean guildDeafened = false;
    private boolean suppressed = false;
    private boolean stream = false;
    private boolean video = false;

    public GuildVoiceStateImpl(Member member) {
        this.api = member.getJFA();
        this.guild = member.getGuild();
        this.member = member;
    }

    @Override
    public boolean isSelfMuted() {
        return selfMuted;
    }

    @Override
    public boolean isSelfDeafened() {
        return selfDeafened;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public long getRequestToSpeak() {
        return requestToSpeak;
    }

    @Override
    public OffsetDateTime getRequestToSpeakTimestamp() {
        return requestToSpeak == 0 ? null : Helpers.toOffset(requestToSpeak);
    }

    @NotNull
    @Override
    public RestAction<Void> approveSpeaker() {
        return update(false);
    }

    @NotNull
    @Override
    public RestAction<Void> declineSpeaker() {
        return update(true);
    }

    private RestAction<Void> update(boolean suppress) {
        if (!(connectedChannel instanceof StageChannel) || suppress == isSuppressed()) {
            return new CompletedRestAction<>(api, null);
        }

        Member selfMember = getGuild().getSelfMember();
        boolean isSelf = selfMember.equals(member);
        if (!isSelf && !selfMember.hasPermission(connectedChannel, Permission.VOICE_MUTE_OTHERS)) {
            throw new InsufficientPermissionException(connectedChannel, Permission.VOICE_MUTE_OTHERS);
        }

        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), isSelf ? "@me" : getId());
        DataObject body =
                DataObject.empty().put("channel_id", connectedChannel.getId()).put("suppress", suppress);
        return new RestActionImpl<>(getJFA(), route, body);
    }

    @NotNull
    @Override
    public RestAction<Void> inviteSpeaker() {
        if (!(connectedChannel instanceof StageChannel)) {
            return new CompletedRestAction<>(api, null);
        }
        if (!getGuild().getSelfMember().hasPermission(connectedChannel, Permission.VOICE_MUTE_OTHERS)) {
            throw new InsufficientPermissionException(connectedChannel, Permission.VOICE_MUTE_OTHERS);
        }

        Route.CompiledRoute route = Route.Guilds.UPDATE_VOICE_STATE.compile(guild.getId(), getId());
        DataObject body = DataObject.empty()
                .put("channel_id", connectedChannel.getId())
                .put("suppress", false)
                .put("request_to_speak_timestamp", OffsetDateTime.now().toString());
        return new RestActionImpl<>(getJFA(), route, body);
    }

    @Override
    public boolean isMuted() {
        return isSelfMuted() || isGuildMuted();
    }

    @Override
    public boolean isDeafened() {
        return isSelfDeafened() || isGuildDeafened();
    }

    @Override
    public boolean isGuildMuted() {
        return guildMuted;
    }

    @Override
    public boolean isGuildDeafened() {
        return guildDeafened;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    public boolean isStream() {
        return stream;
    }

    @Override
    public boolean isSendingVideo() {
        return video;
    }

    @Override
    public AudioChannelUnion getChannel() {
        return (AudioChannelUnion) connectedChannel;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        Guild realGuild = api.getGuildById(guild.getIdLong());
        if (realGuild != null) {
            guild = realGuild;
        }
        return guild;
    }

    @NotNull
    @Override
    public Member getMember() {
        Member realMember = getGuild().getMemberById(member.getIdLong());
        if (realMember != null) {
            member = realMember;
        }
        return member;
    }

    @Override
    public boolean inAudioChannel() {
        return getChannel() != null;
    }

    @Override
    public long getIdLong() {
        return member.getIdLong();
    }

    @Override
    public int hashCode() {
        return member.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GuildVoiceState)) {
            return false;
        }
        GuildVoiceState oStatus = (GuildVoiceState) obj;
        return member.equals(oStatus.getMember());
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("member", getMember()) // Guild metadata is included in Member metadata
                .toString();
    }

    // -- Setters --

    public void setMember(Member member) {
        this.member = member;
    }

    public GuildVoiceStateImpl updateConnectedChannel(AudioChannel connectedChannel) {
        this.connectedChannel = connectedChannel;
        ((GuildImpl) guild).handleVoiceStateUpdate(this);
        return this;
    }

    public GuildVoiceStateImpl setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public GuildVoiceStateImpl setSelfMuted(boolean selfMuted) {
        this.selfMuted = selfMuted;
        return this;
    }

    public GuildVoiceStateImpl setSelfDeafened(boolean selfDeafened) {
        this.selfDeafened = selfDeafened;
        return this;
    }

    public GuildVoiceStateImpl setGuildMuted(boolean guildMuted) {
        this.guildMuted = guildMuted;
        return this;
    }

    public GuildVoiceStateImpl setGuildDeafened(boolean guildDeafened) {
        this.guildDeafened = guildDeafened;
        return this;
    }

    public GuildVoiceStateImpl setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
        return this;
    }

    public GuildVoiceStateImpl setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public GuildVoiceStateImpl setVideo(boolean video) {
        this.video = video;
        return this;
    }

    public GuildVoiceStateImpl setRequestToSpeak(OffsetDateTime timestamp) {
        this.requestToSpeak = timestamp == null ? 0L : timestamp.toInstant().toEpochMilli();
        return this;
    }
}
