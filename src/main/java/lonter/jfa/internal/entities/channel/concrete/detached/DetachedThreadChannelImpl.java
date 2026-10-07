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
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.entities.channel.ChannelFlag;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.entities.channel.unions.IThreadContainerUnion;
import lonter.jfa.api.managers.channel.concrete.ThreadChannelManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import lonter.jfa.api.utils.TimeUtil;
import lonter.jfa.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.ThreadChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;
import lonter.jfa.internal.utils.Helpers;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DetachedThreadChannelImpl extends AbstractGuildChannelImpl<DetachedThreadChannelImpl>
        implements ThreadChannel,
                ThreadChannelMixin<DetachedThreadChannelImpl>,
                IInteractionPermissionMixin<DetachedThreadChannelImpl> {
    private final ChannelType type;
    private ChannelInteractionPermissions interactionPermissions;

    private AutoArchiveDuration autoArchiveDuration;
    private boolean locked;
    private boolean archived;
    private boolean invitable;
    private long archiveTimestamp;
    private long creationTimestamp;
    private long ownerId;
    private long latestMessageId;
    private int messageCount;
    private int totalMessageCount;
    private int memberCount;
    private int slowmode;
    private int flags;

    public DetachedThreadChannelImpl(long id, DetachedGuildImpl guild, ChannelType type) {
        super(id, guild);
        this.type = type;
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public EnumSet<ChannelFlag> getFlags() {
        return ChannelFlag.fromRaw(flags);
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return type;
    }

    @Override
    public long getLatestMessageIdLong() {
        return latestMessageId;
    }

    @Override
    public int getMessageCount() {
        return messageCount;
    }

    @Override
    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    @Override
    public int getMemberCount() {
        return memberCount;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean canTalk(@NotNull Member member) {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        throw detachedException();
    }

    @NotNull
    @Override
    public IThreadContainerUnion getParentChannel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<ForumTag> getAppliedTags() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Message> retrieveParentMessage() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Message> retrieveStartMessage() {
        throw detachedException();
    }

    @NotNull
    @Override
    public IPermissionContainer getPermissionContainer() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<ThreadMember> getThreadMembers() {
        throw detachedException();
    }

    @Nullable
    @Override
    public ThreadMember getThreadMemberById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public CacheRestAction<ThreadMember> retrieveThreadMemberById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ThreadMemberPaginationAction retrieveThreadMembers() {
        throw detachedException();
    }

    @Override
    public long getOwnerIdLong() {
        return ownerId;
    }

    @Override
    public boolean isArchived() {
        return archived;
    }

    @Override
    public boolean isInvitable() {
        if (type != ChannelType.GUILD_PRIVATE_THREAD) {
            throw new UnsupportedOperationException("Only private threads support the concept of invitable.");
        }

        return invitable;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeArchiveInfoLastModified() {
        return Helpers.toOffset(archiveTimestamp);
    }

    @NotNull
    @Override
    public AutoArchiveDuration getAutoArchiveDuration() {
        return autoArchiveDuration;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeCreated() {
        return creationTimestamp == 0 ? TimeUtil.getTimeCreated(getIdLong()) : Helpers.toOffset(creationTimestamp);
    }

    @Override
    public int getSlowmode() {
        return slowmode;
    }

    @NotNull
    @Override
    public RestAction<Void> join() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> leave() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> addThreadMemberById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Void> removeThreadMemberById(long id) {
        throw detachedException();
    }

    @NotNull
    @Override
    public ThreadChannelManager getManager() {
        throw detachedException();
    }

    @Override
    public void checkCanManage() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @Override
    public DetachedThreadChannelImpl setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration) {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setArchived(boolean archived) {
        this.archived = archived;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setInvitable(boolean invitable) {
        this.invitable = invitable;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setArchiveTimestamp(long archiveTimestamp) {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setMessageCount(int messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setTotalMessageCount(int messageCount) {
        this.totalMessageCount = Math.max(messageCount, this.messageCount); // If this is 0 we use the older count
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setMemberCount(int memberCount) {
        this.memberCount = memberCount;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    @Override
    public DetachedThreadChannelImpl setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    @NotNull
    @Override
    public DetachedThreadChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
