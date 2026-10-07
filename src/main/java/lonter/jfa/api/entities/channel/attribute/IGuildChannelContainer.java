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

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.*;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.sharding.ShardManager;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.cache.CacheView;
import lonter.jfa.api.utils.cache.ChannelCacheView;
import lonter.jfa.api.utils.cache.SnowflakeCacheView;
import lonter.jfa.internal.utils.Checks;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides various channel cache getters for Guild channels.
 *
 * <p>These getters only check the caches with the relevant scoping of the implementing type.
 * For example, {@link Guild} returns channels that exist within the guild,
 * whereas {@link JFA} or {@link ShardManager} returns any channels that exist within the shard.
 *
 * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
 * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
 *
 * <p>For the most efficient usage, it is recommended to use {@link CacheView} getters such as {@link #getTextChannelCache()}.
 * List getters usually require making a snapshot copy of the underlying cache view, which may introduce an undesirable performance hit.
 */
public interface IGuildChannelContainer<C extends Channel> {
    /**
     * Unified cache of all channels associated with this shard or guild.
     *
     * <p>This {@link ChannelCacheView} stores all channels in individually typed maps based on {@link ChannelType}.
     * You can use {@link ChannelCacheView#getElementById(ChannelType, long)} or {@link ChannelCacheView#ofType(Class)} to filter
     * out more specific types.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link ChannelCacheView}
     */
    @NotNull
    ChannelCacheView<C> getChannelCache();

    /**
     * Get a channel of the specified type by id.
     *
     * <p>This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     *         {@link Class} of a channel type
     * @param  id
     *         The snowflake id of the channel
     * @param  <T>
     *         The type argument for the class
     *
     * @throws IllegalArgumentException
     *         If null is provided, or the id is not a valid snowflake
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends C> T getChannelById(@NotNull Class<T> type, @NotNull String id) {
        return getChannelById(type, MiscUtil.parseSnowflake(id));
    }

    /**
     * Get a channel of the specified type by id.
     *
     * <p>This will automatically check for all channel types and cast to the specified class.
     * If a channel with the specified id does not exist,
     * or exists but is not an instance of the provided class, this returns null.
     *
     * @param  type
     *         {@link Class} of a channel type
     * @param  id
     *         The snowflake id of the channel
     * @param  <T>
     *         The type argument for the class
     *
     * @throws IllegalArgumentException
     *         If null is provided
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The casted channel, if it exists and is assignable to the provided class, or null
     */
    @Nullable
    default <T extends C> T getChannelById(@NotNull Class<T> type, long id) {
        Checks.notNull(type, "Class");
        return getChannelCache().ofType(type).getElementById(id);
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, String)}</li>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getNewsChannelById(String)}</li>
     *     <li>{@link #getStageChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
     *     <li>{@link #getCategoryById(String)}</li>
     * </ul>
     *
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@NotNull String id) {
        return getGuildChannelById(MiscUtil.parseSnowflake(id));
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, long)}</li>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getNewsChannelById(long)}</li>
     *     <li>{@link #getStageChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
     *     <li>{@link #getCategoryById(long)}</li>
     *     <li>{@link #getForumChannelById(long)}</li>
     * </ul>
     *
     * @param  id
     *         The ID of the channel
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(long id) {
        C channel = getChannelCache().getElementById(id);
        return channel instanceof GuildChannel ? (GuildChannel) channel : null;
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * <br>This is meant for systems that use a dynamic {@link ChannelType} and can
     * profit from a simple function to get the channel instance.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, String)}</li>
     *     <li>{@link #getTextChannelById(String)}</li>
     *     <li>{@link #getNewsChannelById(String)}</li>
     *     <li>{@link #getStageChannelById(String)}</li>
     *     <li>{@link #getVoiceChannelById(String)}</li>
     *     <li>{@link #getCategoryById(String)}</li>
     *     <li>{@link #getForumChannelById(String)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is null
     * @throws java.lang.NumberFormatException
     *         If the provided ID is not a snowflake
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@NotNull ChannelType type, @NotNull String id) {
        return getGuildChannelById(type, MiscUtil.parseSnowflake(id));
    }

    /**
     * Get {@link GuildChannel GuildChannel} for the provided ID.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * <br>This is meant for systems that use a dynamic {@link ChannelType} and can
     * profit from a simple function to get the channel instance.
     *
     * <p>To get more specific channel types you can use one of the following:
     * <ul>
     *     <li>{@link #getChannelById(Class, long)}</li>
     *     <li>{@link #getTextChannelById(long)}</li>
     *     <li>{@link #getNewsChannelById(long)}</li>
     *     <li>{@link #getStageChannelById(long)}</li>
     *     <li>{@link #getVoiceChannelById(long)}</li>
     *     <li>{@link #getCategoryById(long)}</li>
     *     <li>{@link #getForumChannelById(long)}</li>
     * </ul>
     *
     * @param  type
     *         The {@link ChannelType}
     * @param  id
     *         The ID of the channel
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return The GuildChannel or null
     */
    @Nullable
    default GuildChannel getGuildChannelById(@NotNull ChannelType type, long id) {
        C channel = getChannelCache().getElementById(type, id);
        return channel instanceof GuildChannel ? (GuildChannel) channel : null;
    }

    // Stages

    /**
     * Sorted {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link StageChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<StageChannel> getStageChannelCache();

    /**
     * Gets a list of all {@link StageChannel StageChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link StageChannel StageChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all StageChannel names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<StageChannel> getStageChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getStageChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link StageChannel StageChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link StageChannel StageChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(@NotNull String id) {
        return (StageChannel) getChannelCache().getElementById(ChannelType.STAGE, id);
    }

    /**
     * Gets a {@link StageChannel StageChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link StageChannel StageChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link StageChannel StageChannel} with matching id.
     */
    @Nullable
    default StageChannel getStageChannelById(long id) {
        return (StageChannel) getChannelCache().getElementById(ChannelType.STAGE, id);
    }

    /**
     * Gets all {@link StageChannel StageChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getStageChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of {@link StageChannel StageChannels}.
     */
    @NotNull
    @Unmodifiable
    default List<StageChannel> getStageChannels() {
        return getStageChannelCache().asList();
    }

    // Threads

    /**
     * {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link ThreadChannel}.
     *
     * <p>These threads can also represent posts in {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<ThreadChannel> getThreadChannelCache();

    /**
     * Gets a list of all {@link ThreadChannel ThreadChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>These threads can also represent posts in {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link ThreadChannel ThreadChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all ThreadChannel names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<ThreadChannel> getThreadChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getThreadChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>These threads can also represent posts in {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ThreadChannel ThreadChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(@NotNull String id) {
        return (ThreadChannel) getChannelCache().getElementById(ChannelType.GUILD_PUBLIC_THREAD, id);
    }

    /**
     * Gets a {@link ThreadChannel ThreadChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>These threads can also represent posts in {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ThreadChannel ThreadChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link ThreadChannel ThreadChannel} with matching id.
     */
    @Nullable
    default ThreadChannel getThreadChannelById(long id) {
        return (ThreadChannel) getChannelCache().getElementById(ChannelType.GUILD_PUBLIC_THREAD, id);
    }

    /**
     * Gets all {@link ThreadChannel ThreadChannel} in the cache.
     *
     * <p>These threads can also represent posts in {@link lonter.jfa.api.entities.channel.concrete.ForumChannel ForumChannels}.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getThreadChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of {@link ThreadChannel ThreadChannels}.
     */
    @NotNull
    @Unmodifiable
    default List<ThreadChannel> getThreadChannels() {
        return getThreadChannelCache().asList();
    }

    // Categories

    /**
     * Sorted {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link Category}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<Category> getCategoryCache();

    /**
     * Gets a list of all {@link Category Categories}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name to check
     * @param  ignoreCase
     *         Whether to ignore case on name checking
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is {@code null}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Immutable list of all categories matching the provided name
     */
    @NotNull
    @Unmodifiable
    default List<Category> getCategoriesByName(@NotNull String name, boolean ignoreCase) {
        return getCategoryCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link Category Category} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided ID is not a valid {@code long}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(@NotNull String id) {
        return (Category) getChannelCache().getElementById(ChannelType.CATEGORY, id);
    }

    /**
     * Gets a {@link Category Category} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The snowflake ID of the wanted Category
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link Category Category} for the provided ID.
     */
    @Nullable
    default Category getCategoryById(long id) {
        return (Category) getChannelCache().getElementById(ChannelType.CATEGORY, id);
    }

    /**
     * Gets all {@link Category Categories} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getCategoryCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable list of all {@link Category Categories} in this Guild.
     */
    @NotNull
    @Unmodifiable
    default List<Category> getCategories() {
        return getCategoryCache().asList();
    }

    // TextChannels

    /**
     * Sorted {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link TextChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<TextChannel> getTextChannelCache();

    /**
     * Gets a list of all {@link TextChannel TextChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link TextChannel TextChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all TextChannels names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<TextChannel> getTextChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getTextChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link TextChannel TextChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link TextChannel TextChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(@NotNull String id) {
        return (TextChannel) getChannelCache().getElementById(ChannelType.TEXT, id);
    }

    /**
     * Gets a {@link TextChannel TextChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link TextChannel TextChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link TextChannel TextChannel} with matching id.
     */
    @Nullable
    default TextChannel getTextChannelById(long id) {
        return (TextChannel) getChannelCache().getElementById(ChannelType.TEXT, id);
    }

    /**
     * Gets all {@link TextChannel TextChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getTextChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of all {@link TextChannel TextChannels} in this Guild.
     */
    @NotNull
    @Unmodifiable
    default List<TextChannel> getTextChannels() {
        return getTextChannelCache().asList();
    }

    // NewsChannels / AnnouncementChannels

    /**
     * Sorted {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link NewsChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<NewsChannel> getNewsChannelCache();

    /**
     * Gets a list of all {@link NewsChannel NewsChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link NewsChannel NewsChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all NewsChannels names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<NewsChannel> getNewsChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getNewsChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link NewsChannel NewsChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link NewsChannel NewsChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(@NotNull String id) {
        return (NewsChannel) getChannelCache().getElementById(ChannelType.NEWS, id);
    }

    /**
     * Gets a {@link NewsChannel NewsChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link NewsChannel NewsChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link NewsChannel NewsChannel} with matching id.
     */
    @Nullable
    default NewsChannel getNewsChannelById(long id) {
        return (NewsChannel) getChannelCache().getElementById(ChannelType.NEWS, id);
    }

    /**
     * Gets all {@link NewsChannel NewsChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getNewsChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of all {@link NewsChannel NewsChannels} in this Guild.
     */
    @NotNull
    @Unmodifiable
    default List<NewsChannel> getNewsChannels() {
        return getNewsChannelCache().asList();
    }

    // VoiceChannels

    /**
     * Sorted {@link lonter.jfa.api.utils.cache.SnowflakeCacheView SnowflakeCacheView} of {@link VoiceChannel}.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link lonter.jfa.api.utils.cache.SortedSnowflakeCacheView SortedSnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<VoiceChannel> getVoiceChannelCache();

    /**
     * Gets a list of all {@link VoiceChannel VoiceChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link VoiceChannel VoiceChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all VoiceChannel names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<VoiceChannel> getVoiceChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getVoiceChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link VoiceChannel VoiceChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link VoiceChannel VoiceChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(@NotNull String id) {
        return (VoiceChannel) getChannelCache().getElementById(ChannelType.VOICE, id);
    }

    /**
     * Gets a {@link VoiceChannel VoiceChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link VoiceChannel VoiceChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link VoiceChannel VoiceChannel} with matching id.
     */
    @Nullable
    default VoiceChannel getVoiceChannelById(long id) {
        return (VoiceChannel) getChannelCache().getElementById(ChannelType.VOICE, id);
    }

    /**
     * Gets all {@link VoiceChannel VoiceChannels} in the cache.
     * <br>In {@link Guild} cache, channels are sorted according to their position and id.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getVoiceChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of {@link VoiceChannel VoiceChannels}.
     */
    @NotNull
    @Unmodifiable
    default List<VoiceChannel> getVoiceChannels() {
        return getVoiceChannelCache().asList();
    }

    // ForumChannels

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link ForumChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<ForumChannel> getForumChannelCache();

    /**
     * Gets a list of all {@link ForumChannel ForumChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link ForumChannel ForumChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<ForumChannel> getForumChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getForumChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link ForumChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ForumChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link ForumChannel} with matching id.
     */
    @Nullable
    default ForumChannel getForumChannelById(@NotNull String id) {
        return (ForumChannel) getChannelCache().getElementById(ChannelType.FORUM, id);
    }

    /**
     * Gets a {@link ForumChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link ForumChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link ForumChannel} with matching id.
     */
    @Nullable
    default ForumChannel getForumChannelById(long id) {
        return (ForumChannel) getChannelCache().getElementById(ChannelType.FORUM, id);
    }

    /**
     * Gets all {@link ForumChannel} in the cache.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getForumChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of {@link ForumChannel}.
     */
    @NotNull
    @Unmodifiable
    default List<ForumChannel> getForumChannels() {
        return getForumChannelCache().asList();
    }

    // MediaChannels

    /**
     * {@link SnowflakeCacheView SnowflakeCacheView} of {@link MediaChannel}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return {@link SnowflakeCacheView SnowflakeCacheView}
     */
    @NotNull
    SnowflakeCacheView<MediaChannel> getMediaChannelCache();

    /**
     * Gets a list of all {@link MediaChannel MediaChannels}
     * in this Guild that have the same name as the one provided.
     * <br>If there are no channels with the provided name, then this returns an empty list.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  name
     *         The name used to filter the returned {@link MediaChannel MediaChannels}.
     * @param  ignoreCase
     *         Determines if the comparison ignores case when comparing. True - case insensitive.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-empty immutable list of all ForumChannel names that match the provided name.
     */
    @NotNull
    @Unmodifiable
    default List<MediaChannel> getMediaChannelsByName(@NotNull String name, boolean ignoreCase) {
        return getMediaChannelCache().getElementsByName(name, ignoreCase);
    }

    /**
     * Gets a {@link MediaChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link MediaChannel}.
     *
     * @throws java.lang.NumberFormatException
     *         If the provided {@code id} cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link MediaChannel} with matching id.
     */
    @Nullable
    default MediaChannel getMediaChannelById(@NotNull String id) {
        return (MediaChannel) getChannelCache().getElementById(ChannelType.MEDIA, id);
    }

    /**
     * Gets a {@link MediaChannel} that has the same id as the one provided.
     * <br>If there is no channel with an id that matches the provided one, then this returns {@code null}.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @param  id
     *         The id of the {@link MediaChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return Possibly-null {@link MediaChannel} with matching id.
     */
    @Nullable
    default MediaChannel getMediaChannelById(long id) {
        return (MediaChannel) getChannelCache().getElementById(ChannelType.MEDIA, id);
    }

    /**
     * Gets all {@link MediaChannel} in the cache.
     *
     * <p>This copies the backing store into a list. This means every call
     * creates a new list with O(n) complexity. It is recommended to store this into
     * a local variable or use {@link #getForumChannelCache()} and use its more efficient
     * versions of handling these values.
     *
     * <p>This getter exists on any instance of {@link IGuildChannelContainer} and only checks the caches with the relevant scoping.
     * For {@link Guild}, {@link JFA}, or {@link ShardManager},
     * this returns the relevant channel with respect to the cache within each of those objects.
     * For a guild, this would mean it only returns channels within the same guild.
     * <br>If this is called on {@link JFA} or {@link ShardManager}, this may return null immediately after building, because the cache isn't initialized yet.
     * To make sure the cache is initialized after building your {@link JFA} instance, you can use {@link JFA#awaitReady()}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link lonter.jfa.api.entities.detached.IDetachableEntity#isDetached() detached}
     *
     * @return An immutable List of {@link MediaChannel}.
     */
    @NotNull
    @Unmodifiable
    default List<MediaChannel> getMediaChannels() {
        return getMediaChannelCache().asList();
    }
}
