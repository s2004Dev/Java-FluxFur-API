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

package lonter.jfa.internal.entities.channel.concrete;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.managers.channel.concrete.VoiceChannelManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.concrete.VoiceChannelMixin;
import lonter.jfa.internal.managers.channel.concrete.VoiceChannelManagerImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VoiceChannelImpl extends AbstractStandardGuildChannelImpl<VoiceChannelImpl>
        implements VoiceChannel, VoiceChannelMixin<VoiceChannelImpl> {
    private String region;
    private String status = "";
    private long latestMessageId;
    private int bitrate;
    private int userLimit;
    private int slowmode;
    private boolean nsfw;

    public VoiceChannelImpl(long id, GuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    @NotNull
    @Override
    public GuildImpl getGuild() {
        return (GuildImpl) super.getGuild();
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.VOICE;
    }

    @Override
    public int getBitrate() {
        return bitrate;
    }

    @Nullable
    @Override
    public String getRegionRaw() {
        return region;
    }

    @Override
    public int getUserLimit() {
        return userLimit;
    }

    @Override
    public boolean isNSFW() {
        return nsfw;
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @Override
    public long getLatestMessageIdLong() {
        return latestMessageId;
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        return getGuild().getConnectedMembers(this);
    }

    @NotNull
    @Override
    public VoiceChannelManager getManager() {
        return new VoiceChannelManagerImpl(this);
    }

    @NotNull
    @Override
    public String getStatus() {
        return status;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyStatus(@NotNull String status) {
        Checks.notLonger(status, MAX_STATUS_LENGTH, "Voice Status");
        checkCanAccess();
        if (this.equals(getGuild().getSelfMember().getVoiceState().getChannel())) {
            checkPermission(Permission.VOICE_SET_STATUS);
        } else {
            checkCanManage();
        }

        Route.CompiledRoute route = Route.Channels.SET_STATUS.compile(getId());
        DataObject body = DataObject.empty().put("status", status);
        return new AuditableRestActionImpl<>(api, route, body);
    }

    @Override
    public VoiceChannelImpl setBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public VoiceChannelImpl setRegion(String region) {
        this.region = region;
        return this;
    }

    @Override
    public VoiceChannelImpl setUserLimit(int userLimit) {
        this.userLimit = userLimit;
        return this;
    }

    @Override
    public VoiceChannelImpl setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public VoiceChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public VoiceChannelImpl setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public VoiceChannelImpl setStatus(String status) {
        this.status = status;
        return this;
    }
}
