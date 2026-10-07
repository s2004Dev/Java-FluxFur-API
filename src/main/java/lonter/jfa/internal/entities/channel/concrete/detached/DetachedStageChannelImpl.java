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

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.StageInstance;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.managers.channel.concrete.StageChannelManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.StageInstanceAction;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.StageChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;
import lonter.jfa.internal.utils.Checks;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DetachedStageChannelImpl extends AbstractStandardGuildChannelImpl<DetachedStageChannelImpl>
        implements StageChannel,
                StageChannelMixin<DetachedStageChannelImpl>,
                IInteractionPermissionMixin<DetachedStageChannelImpl> {
    private ChannelInteractionPermissions interactionPermissions;

    private String region;
    private int bitrate;
    private int userlimit;
    private int slowmode;
    private boolean ageRestricted;
    private long latestMessageId;

    public DetachedStageChannelImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.STAGE;
    }

    @Override
    public int getBitrate() {
        return bitrate;
    }

    @Override
    public int getUserLimit() {
        return userlimit;
    }

    @Nullable
    @Override
    public String getRegionRaw() {
        return region;
    }

    @Nullable
    @Override
    public StageInstance getStageInstance() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        throw detachedException();
    }

    @NotNull
    @Override
    public StageInstanceAction createStageInstance(@NotNull String topic) {
        throw detachedException();
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @Override
    public boolean isNSFW() {
        return ageRestricted;
    }

    @Override
    public boolean canTalk(@NotNull Member member) {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MESSAGE_SEND);
    }

    @Override
    public long getLatestMessageIdLong() {
        return latestMessageId;
    }

    @NotNull
    @Override
    public StageChannelManager getManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> requestToSpeak() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> cancelRequestToSpeak() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @Override
    public DetachedStageChannelImpl setBitrate(int bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setUserLimit(int userlimit) {
        this.userlimit = userlimit;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setRegion(String region) {
        this.region = region;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setNSFW(boolean ageRestricted) {
        this.ageRestricted = ageRestricted;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedStageChannelImpl setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @NotNull
    @Override
    public DetachedStageChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
