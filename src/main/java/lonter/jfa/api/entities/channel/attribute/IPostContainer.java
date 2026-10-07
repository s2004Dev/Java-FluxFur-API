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

package lonter.jfa.api.entities.channel.attribute;

import lonter.jfa.annotations.Incubating;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelFlag;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.ForumChannel;
import lonter.jfa.api.entities.channel.concrete.MediaChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.entities.emoji.EmojiUnion;
import lonter.jfa.api.managers.channel.attribute.IPostContainerManager;
import lonter.jfa.api.requests.restaction.ForumPostAction;
import lonter.jfa.api.utils.cache.SortedSnowflakeCacheView;
import lonter.jfa.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A channel which contains {@link #createForumPost(String, MessageCreateData) Forum Posts}.
 * <br>Forum posts are simply {@link ThreadChannel ThreadChannels} of type {@link ChannelType#GUILD_PUBLIC_THREAD}.
 *
 * <p>The {@code CREATE POSTS} permission that is shown in the official Fluxer Client, is an alias for {@link lonter.jfa.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
 * {@link lonter.jfa.api.Permission#CREATE_PUBLIC_THREADS Permission.CREATE_PUBLIC_THREADS} is ignored for creating forum posts.
 *
 * @see ForumChannel
 * @see MediaChannel
 * @see #createForumPost(String, MessageCreateData)
 */
public interface IPostContainer extends IThreadContainer {
    /**
     * The maximum length of a forum / media channel topic ({@value})
     */
    int MAX_POST_CONTAINER_TOPIC_LENGTH = 4096;
    /**
     * The maximum number of {@link ForumPostAction#setTags(Collection) tags} that can be applied to a forum post. ({@value})
     */
    int MAX_POST_TAGS = 5;

    @NotNull
    @Override
    @CheckReturnValue
    IPostContainerManager<?, ?> getManager();

    /**
     * The available {@link ForumTag ForumTags} for this forum channel.
     * <br>Tags are sorted by their {@link ForumTag#getPosition() position} ascending.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return {@link SortedSnowflakeCacheView} of {@link ForumTag}
     */
    @NotNull
    SortedSnowflakeCacheView<ForumTag> getAvailableTagCache();

    /**
     * The available {@link ForumTag ForumTags} for this channel.
     * <br>Tags are sorted by their {@link ForumTag#getPosition() position} ascending.
     *
     * <p>This is a shortcut for {@link #getAvailableTagCache() getAvailableTagCache().asList()}.
     * This method will copy the underlying cache into the list, running in {@code O(n)} time.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @return Immutable {@link List} of {@link ForumTag}
     */
    @NotNull
    @Unmodifiable
    default List<ForumTag> getAvailableTags() {
        return getAvailableTagCache().asList();
    }

    /**
     * The available {@link ForumTag ForumTags} for this channel.
     * <br>Tags are sorted by their {@link ForumTag#getPosition() position} ascending.
     *
     * <p>This is a shortcut for {@link #getAvailableTagCache() getAvailableTagCache().getElementsByName(name, ignoreCase)}.
     * This method will copy the underlying cache into the list, running in {@code O(n)} time.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @param  name
     *         The name of the tag
     * @param  ignoreCase
     *         Whether to use {@link String#equalsIgnoreCase(String)}
     *
     * @throws IllegalArgumentException
     *         If the name is {@code null}
     *
     * @return Immutable {@link List} of {@link ForumTag} with the given name
     */
    @NotNull
    @Unmodifiable
    default List<ForumTag> getAvailableTagsByName(@NotNull String name, boolean ignoreCase) {
        return getAvailableTagCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Retrieves the tag for the provided id.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @param  id
     *         The tag id
     *
     * @return The tag for the provided id, or {@code null} if no tag with that id exists
     *
     * @see    lonter.jfa.api.entities.channel.forums.ForumTagSnowflake#fromId(long)
     */
    @Nullable
    default ForumTag getAvailableTagById(long id) {
        return getAvailableTagCache().getElementById(id);
    }

    /**
     * Retrieves the tag for the provided id.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#FORUM_TAGS CacheFlag.FORUM_TAGS} to be enabled.
     *
     * @param  id
     *         The tag id
     *
     * @throws IllegalArgumentException
     *         If the provided id is null
     * @throws NumberFormatException
     *         If the provided id is not a valid snowflake
     *
     * @return The tag for the provided id, or {@code null} if no tag with that id exists
     *
     * @see    lonter.jfa.api.entities.channel.forums.ForumTagSnowflake#fromId(String)
     */
    @Nullable
    default ForumTag getAvailableTagById(@NotNull String id) {
        return getAvailableTagCache().getElementById(id);
    }

    /**
     * The topic set for this channel, this is referred to as <em>Guidelines</em> in the official Fluxer client.
     * <br>If no topic has been set, this returns null.
     *
     * @return Possibly-null String containing the topic of this channel.
     */
    @Nullable
    String getTopic();

    /**
     * Whether all new forum posts must have a tag.
     *
     * @return True, if all new posts must have a tag.
     */
    default boolean isTagRequired() {
        return getFlags().contains(ChannelFlag.REQUIRE_TAG);
    }

    /**
     * The emoji which will show up on new forum posts as default reaction.
     *
     * @return The default reaction for new forum posts.
     */
    @Nullable
    EmojiUnion getDefaultReaction();

    /**
     * The default order used to show threads.
     *
     * @return The default order used to show threads.
     */
    @NotNull
    SortOrder getDefaultSortOrder();

    /**
     * Creates a new forum/media post (thread) in this channel.
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If the channel was deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_AUTOMOD MESSAGE_BLOCKED_BY_AUTOMOD}
     *     <br>If this message was blocked by an {@link lonter.jfa.api.entities.automod.AutoModRule AutoModRule}</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER MESSAGE_BLOCKED_BY_HARMFUL_LINK_FILTER}
     *     <br>If this message was blocked by the harmful link filter</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#REQUEST_ENTITY_TOO_LARGE REQUEST_ENTITY_TOO_LARGE}
     *     <br>If the total sum of uploaded bytes exceeds the guild's {@link Guild#getMaxFileSize() upload limit}</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#TITLE_BLOCKED_BY_AUTOMOD TITLE_BLOCKED_BY_AUTOMOD}
     *     <br>If the forum post name was blocked by an {@link lonter.jfa.api.entities.automod.AutoModRule AutoModRule}</li>
     * </ul>
     *
     * @param  name
     *         The name of the post (up to {@value Channel#MAX_NAME_LENGTH} characters)
     * @param  message
     *         The starting message of the post (see {@link lonter.jfa.api.utils.messages.MessageCreateBuilder MessageCreateBuilder})
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the bot does not have {@link lonter.jfa.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND} in the channel
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If null is provided</li>
     *             <li>If the name is empty or longer than {@value Channel#MAX_NAME_LENGTH} characters</li>
     *         </ul>
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link ForumPostAction}
     */
    @NotNull
    @Incubating
    @CheckReturnValue
    ForumPostAction createForumPost(@NotNull String name, @NotNull MessageCreateData message);

    /**
     * The order used to sort forum posts.
     */
    enum SortOrder {
        /**
         * Sort by recent activity, including unarchive, message, reaction, and thread creation.
         */
        RECENT_ACTIVITY(0),
        /**
         * Sort by the time the post was originally created.
         */
        CREATION_TIME(1),
        /**
         * Placeholder for possible future order modes.
         */
        UNKNOWN(-1),
        ;

        private final int order;

        SortOrder(int order) {
            this.order = order;
        }

        /**
         * The underlying value as used by Fluxer.
         *
         * @return The raw order key
         */
        public int getKey() {
            return order;
        }

        /**
         * The {@link SortOrder} for the provided key.
         *
         * @param  key
         *         The key to get the {@link SortOrder} for
         *
         * @return The {@link SortOrder} for the provided key, or {@link #UNKNOWN} if the key is not known
         */
        @NotNull
        public static SortOrder fromKey(int key) {
            for (SortOrder order : values()) {
                if (order.order == key) {
                    return order;
                }
            }

            return UNKNOWN;
        }
    }
}
