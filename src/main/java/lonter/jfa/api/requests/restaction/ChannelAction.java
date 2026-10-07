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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.Permission;
import lonter.jfa.api.Region;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.attribute.ISlowmodeChannel;
import lonter.jfa.api.entities.channel.attribute.IThreadContainer;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.StageChannel;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.entities.channel.forums.BaseForumTag;
import lonter.jfa.api.entities.channel.forums.ForumTagData;
import lonter.jfa.api.entities.channel.middleman.AudioChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.StandardGuildMessageChannel;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link lonter.jfa.api.requests.RestAction RestAction} specifically
 * designed to create a {@link GuildChannel GuildChannel}.
 * This extension allows setting properties before executing the action.
 *
 * @param <T>
 *        The type of channel to create
 *
 * @see    lonter.jfa.api.entities.Guild
 * @see    lonter.jfa.api.entities.Guild#createTextChannel(String)
 * @see    lonter.jfa.api.entities.Guild#createNewsChannel(String)
 * @see    lonter.jfa.api.entities.Guild#createVoiceChannel(String)
 * @see    lonter.jfa.api.entities.Guild#createStageChannel(String)
 * @see    lonter.jfa.api.entities.Guild#createCategory(String)
 * @see    lonter.jfa.api.entities.channel.attribute.ICopyableChannel#createCopy()
 * @see    lonter.jfa.api.entities.channel.attribute.ICopyableChannel#createCopy(Guild)
 */
public interface ChannelAction<T extends GuildChannel> extends FluentAuditableRestAction<T, ChannelAction<T>> {
    /**
     * The guild to create this {@link GuildChannel} in
     *
     * @return The guild
     */
    @NotNull
    Guild getGuild();

    /**
     * The {@link ChannelType} for the resulting channel
     *
     * @return The channel type
     */
    @NotNull
    ChannelType getType();

    /**
     * Sets the name for the new GuildChannel
     *
     * @param  name
     *         The not-null name for the new GuildChannel (1-{@value Channel#MAX_NAME_LENGTH} characters long)
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is null or not between 1-{@value Channel#MAX_NAME_LENGTH} characters long
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setName(@NotNull String name);

    /**
     * Sets the {@link Category Category} for the new GuildChannel.
     *
     * <p>You can use {@link #syncPermissionOverrides()} to sync the channel with the category.
     *
     * @param  category
     *         The parent for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is for a Category
     * @throws IllegalArgumentException
     *         If the provided category is {@code null}
     *         or not from this Guild
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    #syncPermissionOverrides()
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setParent(@Nullable Category category);

    /**
     * Sets the position where the new Channel should be inserted into.
     * This refers to the raw position value, not the computed (relative) position.
     * <p>
     * By default (or by providing this method with {@code null}),
     * the position will automatically be computed based on the other Channels (inserted last in its respective group).
     * <p>
     * Note: This does not shift the position values of existing Channels if the values collide.
     * <br>As a reminder: The ordering of Channels is determined first by its Category's position, then by its raw
     * position value and finally by its id (younger Channels are below older ones)
     *
     * @param  position
     *         The raw position value that should be used for the new Channel
     *
     * @throws IllegalArgumentException
     *         If the provided position value is {@code <0}
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setPosition(@Nullable Integer position);

    /**
     * Sets the topic for the channel
     *
     * @param  topic
     *         The topic for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     * @throws IllegalArgumentException
     *         If the provided topic is greater than {@value StandardGuildMessageChannel#MAX_TOPIC_LENGTH} in length.
     *         For {@link IPostContainer IPostContainers},
     *         this limit is {@value IPostContainer#MAX_POST_CONTAINER_TOPIC_LENGTH} instead.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setTopic(@Nullable String topic);

    /**
     * Sets the NSFW flag for the channel
     *
     * @param  nsfw
     *         The NSFW flag for the new GuildChannel
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a TextChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setNSFW(boolean nsfw);

    /**
     * Sets the slowmode value, which limits the amount of time that individual users must wait
     * between sending messages in the new channel. This is measured in seconds.
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having the {@link Permission#BYPASS_SLOWMODE BYPASS_SLOWMODE} permission also grants immunity to slowmode.
     *
     * @param  slowmode
     *         The number of seconds required to wait between sending messages in the channel.
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a {@link ISlowmodeChannel}
     * @throws IllegalArgumentException
     *         If the {@code slowmode} is greater than {@link ISlowmodeChannel#MAX_SLOWMODE ISlowmodeChannel.MAX_SLOWMODE}, or less than 0
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setSlowmode(int slowmode);

    /**
     * Sets the slowmode value, which limits the amount of time that individual users must wait
     * between sending messages in the new channel. This is measured in seconds.
     * <br>This is applied to newly created threads by default.
     *
     * <p>Note: Bots are unaffected by this.
     * <br>Having the {@link Permission#BYPASS_SLOWMODE BYPASS_SLOWMODE} permission also grants immunity to slowmode.
     *
     * @param  slowmode
     *         The number of seconds required to wait between sending messages in the channel.
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a {@link IThreadContainer}
     * @throws IllegalArgumentException
     *         If the {@code slowmode} is greater than {@link ISlowmodeChannel#MAX_SLOWMODE ISlowmodeChannel.MAX_SLOWMODE}, or less than 0
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setDefaultThreadSlowmode(int slowmode);

    /**
     * Sets the <b><u>default reaction emoji</u></b> of the channel.
     * <br>This does not support custom emoji from other guilds.
     *
     * @param  emoji
     *         The new default reaction emoji, or null to unset.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    IPostContainer#getDefaultReaction()
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setDefaultReaction(@Nullable Emoji emoji);

    /**
     * Sets the <b><u>default layout</u></b> of the new {@link ForumChannel}.
     *
     * @param  layout
     *         The new default layout.
     *
     * @throws IllegalArgumentException
     *         If null or {@link lonter.jfa.api.entities.channel.concrete.ForumChannel.Layout#UNKNOWN UNKNOWN} is provided
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    ForumChannel#getDefaultLayout()
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setDefaultLayout(@NotNull ForumChannel.Layout layout);

    /**
     * Sets the <b><u>default sort order</u></b> of the channel.
     *
     * @param  sortOrder
     *         The new default sort order.
     *
     * @throws IllegalArgumentException
     *         If null or {@link lonter.jfa.api.entities.channel.attribute.IPostContainer.SortOrder#UNKNOWN UNKNOWN} is provided
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    IPostContainer#getDefaultSortOrder()
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setDefaultSortOrder(@NotNull IPostContainer.SortOrder sortOrder);

    /**
     * Sets the <b><u>available tags</u></b> of the channel.
     * <br>Tags will be ordered based on the provided list order.
     *
     * <p>You can use {@link ForumTagData} to create new tags.
     *
     * @param  tags
     *         The new available tags in the desired order.
     *
     * @throws IllegalArgumentException
     *         If the provided list is null or contains null elements
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    IPostContainer#getAvailableTags()
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setAvailableTags(@NotNull List<? extends BaseForumTag> tags);

    /**
     * Adds a new Role or Member {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * Role role = guild.getPublicRole();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addPermissionOverride(role, allow, deny);
     * }
     *
     * @param  target
     *         The not-null {@link lonter.jfa.api.entities.Role Role} or {@link lonter.jfa.api.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override or null
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     * @throws java.lang.IllegalArgumentException
     *         If the specified target is null or not within the same guild.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    java.util.EnumSet
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> addPermissionOverride(
            @NotNull IPermissionHolder target,
            @Nullable Collection<Permission> allow,
            @Nullable Collection<Permission> deny) {
        long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addPermissionOverride(target, allowRaw, denyRaw);
    }

    /**
     * Adds a new Role or Member {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * Role role = guild.getPublicRole();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addPermissionOverride(role, allow, deny);
     * }
     *
     * @param  target
     *         The not-null {@link lonter.jfa.api.entities.Role Role} or {@link lonter.jfa.api.entities.Member Member} for the override
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If the specified target is null
     *                 or not within the same guild.</li>
     *             <li>If one of the provided Permission values is invalid</li>
     *         </ul>
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRawValue()
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection)
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...)
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> addPermissionOverride(@NotNull IPermissionHolder target, long allow, long deny) {
        Checks.notNull(target, "Override Role/Member");
        if (target instanceof Role) {
            return addRolePermissionOverride(target.getIdLong(), allow, deny);
        } else if (target instanceof Member) {
            return addMemberPermissionOverride(target.getIdLong(), allow, deny);
        }
        throw new IllegalArgumentException(
                "Cannot add override for " + target.getClass().getSimpleName());
    }

    /**
     * Adds a new Member {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * long userId = user.getIdLong();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addMemberPermissionOverride(userId, allow, deny);
     * }
     *
     * @param  memberId
     *         The id for the member
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override or null
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    java.util.EnumSet
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> addMemberPermissionOverride(
            long memberId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny) {
        long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addMemberPermissionOverride(memberId, allowRaw, denyRaw);
    }

    /**
     * Adds a new Role {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     * for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * long roleId = role.getIdLong();
     * EnumSet<Permission> allow = EnumSet.of(Permission.VIEW_CHANNEL);
     * EnumSet<Permission> deny = EnumSet.of(Permission.MESSAGE_SEND);
     * channelAction.addRolePermissionOverride(roleId, allow, deny);
     * }
     *
     * @param  roleId
     *         The id for the role
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override or null
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override or null
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    java.util.EnumSet
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> addRolePermissionOverride(
            long roleId, @Nullable Collection<Permission> allow, @Nullable Collection<Permission> deny) {
        long allowRaw = allow != null ? Permission.getRaw(allow) : 0;
        long denyRaw = deny != null ? Permission.getRaw(deny) : 0;

        return addRolePermissionOverride(roleId, allowRaw, denyRaw);
    }

    /**
     * Adds a new Member {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * long userId = user.getIdLong();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addMemberPermissionOverride(userId, allow, deny);
     * }
     *
     * @param  memberId
     *         The id for the member
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     *         If one of the provided Permission values is invalid
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRawValue()
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection)
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...)
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> addMemberPermissionOverride(long memberId, long allow, long deny);

    /**
     * Adds a new Role {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} for the new GuildChannel.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     *
     * <p>Example:
     * {@snippet lang="java":
     * long roleId = role.getIdLong();
     * long allow = Permission.VIEW_CHANNEL.getRawValue();
     * long deny = Permission.MESSAGE_SEND.getRawValue() | Permission.MESSAGE_ADD_REACTION.getRawValue();
     * channelAction.addMemberPermissionOverride(roleId, allow, deny);
     * }
     *
     * @param  roleId
     *         The id for the role
     * @param  allow
     *         The granted {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     * @param  deny
     *         The denied {@link lonter.jfa.api.Permission Permissions} for the override.
     *         Use {@link lonter.jfa.api.Permission#getRawValue()} to retrieve these Permissions.
     *
     * @throws java.lang.IllegalArgumentException
     *         If one of the provided Permission values is invalid
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If any permission is set in allow/deny that the currently logged in account is missing,
     *         unless {@link Permission#MANAGE_PERMISSIONS} or {@link Permission#MANAGE_ROLES} is granted to it within the context of the parent category.
     *
     * @return The current ChannelAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRawValue()
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection)
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...)
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> addRolePermissionOverride(long roleId, long allow, long deny);

    /**
     * Removes any existing override with the provided id.
     * <br>If no override with the provided id exists, this method does nothing.
     *
     * @param  id
     *         The member or role id of the override
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> removePermissionOverride(long id);

    /**
     * Removes any existing override with the provided id.
     * <br>If no override with the provided id exists, this method does nothing.
     *
     * @param  id
     *         The member or role id of the override
     *
     * @throws IllegalArgumentException
     *         If the provided string is not a valid snowflake or null
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> removePermissionOverride(@NotNull String id) {
        return removePermissionOverride(MiscUtil.parseSnowflake(id));
    }

    /**
     * Removes any existing override with the provided role/member.
     * <br>If no override for the provided role/member exists, this method does nothing.
     *
     * @param  holder
     *         The member or role of the override
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is null
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default ChannelAction<T> removePermissionOverride(@NotNull IPermissionHolder holder) {
        Checks.notNull(holder, "PermissionHolder");
        return removePermissionOverride(holder.getIdLong());
    }

    /**
     * Removes all currently configured permission overrides
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> clearPermissionOverrides();

    /**
     * Syncs the permission overrides of the channel with the category.
     *
     * <p>If setting permission overwrites, only permissions your bot has in the guild can be allowed/denied.
     * In order to properly sync permissions the currently logged in account must have all allowed/denied permissions or {@link Permission#MANAGE_ROLES} in the parent category.
     *
     * @throws IllegalArgumentException
     *         If no parent has been configured. You have to use {@link #setParent(Category)} before calling this method.
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> syncPermissionOverrides();

    /**
     * Sets the bitrate for the new VoiceChannel
     *
     * @param  bitrate
     *         The bitrate for the new VoiceChannel in {@code bps} (limits 8000 {@literal <}= bitrate {@literal <}= {@link Guild#getMaxBitrate()})
     *         or {@code null} to use the default 64kbps.
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a VoiceChannel
     * @throws IllegalArgumentException
     *         If the provided bitrate is less than 8000 or greater than {@link lonter.jfa.api.entities.Guild#getMaxBitrate()}
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setBitrate(@Nullable Integer bitrate);

    /**
     * Sets the userlimit for the new {@link AudioChannel}.
     * <br>The limit maximum varies by type.
     * <ul>
     *     <li>{@link ChannelType#VOICE} - {@value VoiceChannel#MAX_USERLIMIT}</li>
     *     <li>{@link ChannelType#STAGE} - {@value StageChannel#MAX_USERLIMIT}</li>
     * </ul>
     *
     * @param  userlimit
     *         The userlimit for the new AudioChannel or {@code null}/{@code 0} to use no limit
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for a AudioChannel
     * @throws IllegalArgumentException
     *         If the provided userlimit is negative or above the permitted limit
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setUserlimit(@Nullable Integer userlimit);

    /**
     * Sets the voice region for the new AudioChannel
     *
     * @param  region
     *         The region for the new AudioChannel, or {@code null} to set to {@link Region#AUTOMATIC}
     *
     * @throws UnsupportedOperationException
     *         If this ChannelAction is not for an AudioChannel
     *
     * @return The current ChannelAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<T> setRegion(@Nullable Region region);
}
