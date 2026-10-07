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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.ThreadMember;
import lonter.jfa.api.entities.channel.ChannelFlag;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.attribute.IThreadContainer;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.entities.channel.unions.IThreadContainerUnion;
import lonter.jfa.api.managers.channel.concrete.ThreadChannelManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.requests.restaction.pagination.ThreadMemberPaginationAction;
import lonter.jfa.api.utils.TimeUtil;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.channel.middleman.AbstractGuildChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.concrete.ThreadChannelMixin;
import lonter.jfa.internal.managers.channel.concrete.ThreadChannelManagerImpl;
import lonter.jfa.internal.requests.DeferredRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.pagination.ThreadMemberPaginationActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.LongStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThreadChannelImpl extends AbstractGuildChannelImpl<ThreadChannelImpl>
        implements ThreadChannel, ThreadChannelMixin<ThreadChannelImpl> {
    private final ChannelType type;
    private final CacheView.SimpleCacheView<ThreadMember> threadMembers =
            new CacheView.SimpleCacheView<>(ThreadMember.class, null);

    private TLongSet appliedTags = new TLongHashSet(ForumChannel.MAX_POST_TAGS);
    private AutoArchiveDuration autoArchiveDuration;
    private IThreadContainerUnion parentChannel;
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

    public ThreadChannelImpl(long id, GuildImpl guild, ChannelType type) {
        super(id, guild);
        this.type = type;
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
        Checks.notNull(member, "Member");
        if (type == ChannelType.GUILD_PRIVATE_THREAD && threadMembers.get(member.getIdLong()) == null) {
            return member.hasPermission(
                    getParentChannel(), Permission.MANAGE_THREADS, Permission.MESSAGE_SEND_IN_THREADS);
        }
        return member.hasPermission(getParentChannel(), Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND_IN_THREADS);
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public IThreadContainerUnion getParentChannel() {
        IThreadContainer realChannel = getGuild().getChannelById(IThreadContainer.class, parentChannel.getIdLong());
        if (realChannel != null) {
            parentChannel = (IThreadContainerUnion) realChannel;
        }
        return parentChannel;
    }

    @NotNull
    @Override
    public List<ForumTag> getAppliedTags() {
        IThreadContainerUnion parent = getParentChannel();
        if (parent.getType() != ChannelType.FORUM) {
            return Collections.emptyList();
        }
        return parent.asForumChannel().getAvailableTagCache().stream()
                .filter(tag -> this.appliedTags.contains(tag.getIdLong()))
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public RestAction<Message> retrieveParentMessage() {
        return this.getParentMessageChannel().retrieveMessageById(this.getIdLong());
    }

    @NotNull
    @Override
    public RestAction<Message> retrieveStartMessage() {
        return retrieveMessageById(getId());
    }

    @NotNull
    @Override
    public IPermissionContainer getPermissionContainer() {
        return getParentChannel();
    }

    @NotNull
    @Override
    public List<ThreadMember> getThreadMembers() {
        return threadMembers.asList();
    }

    @Nullable
    @Override
    public ThreadMember getThreadMemberById(long id) {
        return threadMembers.get(id);
    }

    @NotNull
    @Override
    public CacheRestAction<ThreadMember> retrieveThreadMemberById(long id) {
        JFAImpl jfa = (JFAImpl) getJFA();
        return new DeferredRestAction<>(jfa, ThreadMember.class, () -> getThreadMemberById(id), () -> {
            Route.CompiledRoute route = Route.Channels.GET_THREAD_MEMBER
                    .compile(getId(), Long.toUnsignedString(id))
                    .withQueryParams("with_member", "true");
            return new RestActionImpl<>(jfa, route, (resp, req) -> jfa.getEntityBuilder()
                    .createThreadMember(getGuild(), this, resp.getObject()));
        });
    }

    @NotNull
    @Override
    public ThreadMemberPaginationAction retrieveThreadMembers() {
        return new ThreadMemberPaginationActionImpl(this);
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
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.JOIN_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @NotNull
    @Override
    public RestAction<Void> leave() {
        checkUnarchived();

        Route.CompiledRoute route = Route.Channels.LEAVE_THREAD.compile(getId());
        return new RestActionImpl<>(api, route);
    }

    @NotNull
    @Override
    public RestAction<Void> addThreadMemberById(long id) {
        checkUnarchived();
        checkInvitable();
        checkPermission(Permission.MESSAGE_SEND_IN_THREADS);

        Route.CompiledRoute route = Route.Channels.ADD_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @NotNull
    @Override
    public RestAction<Void> removeThreadMemberById(long id) {
        checkUnarchived();

        boolean privateThreadOwner = type == ChannelType.GUILD_PRIVATE_THREAD
                && ownerId == api.getSelfUser().getIdLong();
        if (!privateThreadOwner) {
            checkPermission(Permission.MANAGE_THREADS);
        }

        Route.CompiledRoute route = Route.Channels.REMOVE_THREAD_MEMBER.compile(getId(), Long.toUnsignedString(id));
        return new RestActionImpl<>(api, route);
    }

    @NotNull
    @Override
    public ThreadChannelManager getManager() {
        return new ThreadChannelManagerImpl(this);
    }

    @Override
    public void checkCanManage() {
        if (isOwner()) {
            return;
        }

        checkPermission(Permission.MANAGE_THREADS);
    }

    public CacheView.SimpleCacheView<ThreadMember> getThreadMemberView() {
        return threadMembers;
    }

    @Override
    public ThreadChannelImpl setLatestMessageIdLong(long latestMessageId) {
        this.latestMessageId = latestMessageId;
        return this;
    }

    @Override
    public ThreadChannelImpl setAutoArchiveDuration(AutoArchiveDuration autoArchiveDuration) {
        this.autoArchiveDuration = autoArchiveDuration;
        return this;
    }

    public ThreadChannelImpl setParentChannel(IThreadContainer channel) {
        this.parentChannel = (IThreadContainerUnion) channel;
        return this;
    }

    @Override
    public ThreadChannelImpl setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    @Override
    public ThreadChannelImpl setArchived(boolean archived) {
        this.archived = archived;
        return this;
    }

    @Override
    public ThreadChannelImpl setInvitable(boolean invitable) {
        this.invitable = invitable;
        return this;
    }

    @Override
    public ThreadChannelImpl setArchiveTimestamp(long archiveTimestamp) {
        this.archiveTimestamp = archiveTimestamp;
        return this;
    }

    @Override
    public ThreadChannelImpl setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
        return this;
    }

    @Override
    public ThreadChannelImpl setOwnerId(long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public ThreadChannelImpl setMessageCount(int messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    @Override
    public ThreadChannelImpl setTotalMessageCount(int messageCount) {
        this.totalMessageCount = Math.max(messageCount, this.messageCount); // If this is 0 we use the older count
        return this;
    }

    @Override
    public ThreadChannelImpl setMemberCount(int memberCount) {
        this.memberCount = memberCount;
        return this;
    }

    @Override
    public ThreadChannelImpl setSlowmode(int slowmode) {
        this.slowmode = slowmode;
        return this;
    }

    public ThreadChannelImpl setAppliedTags(LongStream tags) {
        TLongSet set = new TLongHashSet(ForumChannel.MAX_POST_TAGS);
        tags.forEach(set::add);
        this.appliedTags = set;
        return this;
    }

    @Override
    public ThreadChannelImpl setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public long getArchiveTimestamp() {
        return archiveTimestamp;
    }

    public TLongSet getAppliedTagsSet() {
        return appliedTags;
    }

    public int getRawFlags() {
        return flags;
    }

    private void checkUnarchived() {
        if (archived) {
            throw new IllegalStateException("Cannot modify a ThreadChannel while it is archived!");
        }
    }

    private void checkInvitable() {
        if (ownerId == api.getSelfUser().getIdLong()) {
            return;
        }

        if (!isPublic() && !isInvitable()) {
            checkPermission(Permission.MANAGE_THREADS);
        }
    }
}
