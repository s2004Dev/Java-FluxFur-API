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

package lonter.jfa.internal.entities.channel.concrete.detached;

import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.managers.channel.concrete.VoiceChannelManager;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.VoiceChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DetachedVoiceChannelImpl extends AbstractStandardGuildChannelImpl<DetachedVoiceChannelImpl>
        implements VoiceChannel,
                VoiceChannelMixin<DetachedVoiceChannelImpl>,
                IInteractionPermissionMixin<DetachedVoiceChannelImpl> {
    private ChannelInteractionPermissions interactionPermissions;

    private String region;
    private String status = "";
    private long latestMessageId;
    private int bitrate;
    private int userLimit;
    private int slowmode;
    private boolean nsfw;

    public DetachedVoiceChannelImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
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
        throw detachedException();
    }

    @NotNull
    @Override
    public VoiceChannelManager getManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public String getStatus() {
        return status;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyStatus(@NotNull String status) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @Override
    public DetachedVoiceChannelImpl setBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setRegion(String region) {
        this.region = region;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setUserLimit(int userLimit) {
        this.userLimit = userLimit;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setNSFW(boolean nsfw) {
        this.nsfw = nsfw;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public DetachedVoiceChannelImpl setStatus(String status) {
        this.status = status;
        return this;
    }

    @NotNull
    @Override
    public DetachedVoiceChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
