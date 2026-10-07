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

package lonter.jfa.internal.entities.detached;

import lonter.jfa.api.JFA;
import lonter.jfa.api.OnlineStatus;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.exceptions.MissingEntityInteractionPermissionsException;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.mixin.MemberMixin;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;
import lonter.jfa.internal.interactions.MemberInteractionPermissions;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DetachedMemberImpl implements Member, MemberMixin<DetachedMemberImpl> {
    private final JFAImpl api;

    private final DetachedGuildImpl guild;
    private User user;
    private String nickname;
    private String avatarId;
    private long joinDate, boostDate, timeOutEnd;
    private boolean pending = false;
    private int flags;

    // Permissions calculated by Fluxer
    private MemberInteractionPermissions interactionPermissions;

    public DetachedMemberImpl(DetachedGuildImpl guild, User user) {
        this.api = (JFAImpl) user.getJFA();
        this.guild = guild;
        this.user = user;
        this.joinDate = 0;
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public User getUser() {
        // The user could come from another guild
        // Load user from cache if one exists,
        // ideally two members with the same id should wrap the same user object
        User realUser = getJFA().getUserById(user.getIdLong());
        if (realUser != null) {
            this.user = realUser;
        }
        return user;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @NotNull
    @Override
    public OffsetDateTime getTimeJoined() {
        if (hasTimeJoined()) {
            return Helpers.toOffset(joinDate);
        }
        return getGuild().getTimeCreated();
    }

    @Override
    public boolean hasTimeJoined() {
        return joinDate != 0;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeBoosted() {
        return isBoosting() ? Helpers.toOffset(boostDate) : null;
    }

    @Override
    public boolean isBoosting() {
        return boostDate != 0;
    }

    @Nullable
    @Override
    public OffsetDateTime getTimeOutEnd() {
        return timeOutEnd != 0 ? Helpers.toOffset(timeOutEnd) : null;
    }

    @Override
    public GuildVoiceState getVoiceState() {
        throw detachedException();
    }

    @NotNull
    @Override
    public List<Activity> getActivities() {
        throw detachedException();
    }

    @NotNull
    @Override
    public OnlineStatus getOnlineStatus() {
        throw detachedException();
    }

    @NotNull
    @Override
    public OnlineStatus getOnlineStatus(@NotNull ClientType type) {
        throw detachedException();
    }

    @NotNull
    @Override
    public EnumSet<ClientType> getActiveClients() {
        throw detachedException();
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getAvatarId() {
        return avatarId;
    }

    @NotNull
    @Override
    public String getEffectiveName() {
        return nickname != null ? nickname : getUser().getEffectiveName();
    }

    @NotNull
    @Override
    public List<Role> getRoles() {
        throw detachedException();
    }

    @NotNull
    @Override
    public Set<Role> getUnsortedRoles() {
        throw detachedException();
    }

    @Override
    public int getFlagsRaw() {
        return flags;
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions() {
        throw detachedRequiresChannelException();
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissions(@NotNull GuildChannel channel) {
        return Permission.getPermissions(getRawInteractionPermissions(channel));
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit() {
        throw detachedRequiresChannelException();
    }

    @NotNull
    @Override
    public EnumSet<Permission> getPermissionsExplicit(@NotNull GuildChannel channel) {
        return Permission.getPermissions(getRawInteractionPermissions(channel));
    }

    @Override
    public boolean hasPermission(@NotNull Permission... permissions) {
        throw detachedRequiresChannelException();
    }

    @Override
    public boolean hasPermission(@NotNull GuildChannel channel, @NotNull Permission... permissions) {
        long rawPermissions = Permission.getRaw(permissions);
        return (getRawInteractionPermissions(channel) & rawPermissions) == rawPermissions;
    }

    private long getRawInteractionPermissions(@NotNull GuildChannel channel) {
        if (interactionPermissions.getChannelId() == channel.getIdLong()) {
            return interactionPermissions.getPermissions();
        }

        if (channel instanceof IInteractionPermissionMixin<?>) {
            ChannelInteractionPermissions channelInteractionPermissions =
                    ((IInteractionPermissionMixin<?>) channel).getInteractionPermissions();
            if (channelInteractionPermissions.getMemberId() == this.getIdLong()) {
                return channelInteractionPermissions.getPermissions();
            }
        }

        throw new MissingEntityInteractionPermissionsException(
                "Detached member permissions can only be retrieved in the interaction channel, "
                        + "and channels only contain the permissions of the interaction caller");
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer targetChannel, @NotNull IPermissionContainer syncSource) {
        throw detachedException();
    }

    @Override
    public boolean canSync(@NotNull IPermissionContainer channel) {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@NotNull Member member) {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@NotNull Role role) {
        throw detachedException();
    }

    @Override
    public boolean canInteract(@NotNull RichCustomEmoji emoji) {
        throw detachedException();
    }

    @Override
    public boolean isOwner() {
        throw detachedException();
    }

    @Override
    public boolean isPending() {
        return this.pending;
    }

    @Override
    public long getIdLong() {
        return user.getIdLong();
    }

    @NotNull
    @Override
    public String getAsMention() {
        return user.getAsMention();
    }

    @Nullable
    @Override
    public DefaultGuildChannelUnion getDefaultChannel() {
        throw detachedException();
    }

    @NotNull
    @Override
    public String getDefaultAvatarId() {
        return user.getDefaultAvatarId();
    }

    @NotNull
    public MemberInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> modifyFlags(@NotNull Collection<MemberFlag> newFlags) {
        throw detachedException();
    }

    @Override
    public DetachedMemberImpl setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    @Override
    public DetachedMemberImpl setAvatarId(String avatarId) {
        this.avatarId = avatarId;
        return this;
    }

    @Override
    public DetachedMemberImpl setJoinDate(long joinDate) {
        this.joinDate = joinDate;
        return this;
    }

    @Override
    public DetachedMemberImpl setBoostDate(long boostDate) {
        this.boostDate = boostDate;
        return this;
    }

    @Override
    public DetachedMemberImpl setTimeOutEnd(long time) {
        this.timeOutEnd = time;
        return this;
    }

    @Override
    public DetachedMemberImpl setPending(boolean pending) {
        this.pending = pending;
        return this;
    }

    @Override
    public DetachedMemberImpl setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    public DetachedMemberImpl setInteractionPermissions(@NotNull MemberInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }

    public long getBoostDateRaw() {
        return boostDate;
    }

    public long getTimeOutEndRaw() {
        return timeOutEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DetachedMemberImpl)) {
            return false;
        }

        DetachedMemberImpl oMember = (DetachedMemberImpl) o;
        return oMember.user.getIdLong() == user.getIdLong() && oMember.guild.getIdLong() == guild.getIdLong();
    }

    @Override
    public int hashCode() {
        return Objects.hash(guild.getIdLong(), user.getIdLong());
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setName(getEffectiveName())
                .addMetadata("user", getUser())
                .addMetadata("guild", getGuild())
                .toString();
    }
}
