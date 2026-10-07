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

package lonter.jfa.api.exceptions;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the user is missing a {@link Permission} for some action.
 *
 * @see   lonter.jfa.api.entities.IPermissionHolder#hasPermission(Permission...) IPermissionHolder.hasPermission(Permission...)
 * @see   lonter.jfa.api.entities.IPermissionHolder#hasPermission(GuildChannel, Permission...) IPermissionHolder.hasPermission(GuildChannel, Permission...)
 */
public class InsufficientPermissionException extends PermissionException {
    private final long guildId;
    private final long channelId;
    private final ChannelType channelType;

    public InsufficientPermissionException(@NotNull Guild guild, @NotNull Permission permission) {
        this(guild, null, permission);
    }

    public InsufficientPermissionException(
            @NotNull Guild guild, @NotNull Permission permission, @NotNull String reason) {
        this(guild, null, permission, reason);
    }

    public InsufficientPermissionException(@NotNull GuildChannel channel, @NotNull Permission permission) {
        this(channel.getGuild(), channel, permission);
    }

    public InsufficientPermissionException(
            @NotNull GuildChannel channel, @NotNull Permission permission, @NotNull String reason) {
        this(channel.getGuild(), channel, permission, reason);
    }

    private InsufficientPermissionException(
            @NotNull Guild guild, @Nullable GuildChannel channel, @NotNull Permission permission) {
        super(
                permission,
                "Cannot perform action due to a lack of Permission. Missing permission: " + permission.toString());
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
        this.channelType = channel == null ? ChannelType.UNKNOWN : channel.getType();
    }

    private InsufficientPermissionException(
            @NotNull Guild guild,
            @Nullable GuildChannel channel,
            @NotNull Permission permission,
            @NotNull String reason) {
        super(permission, reason);
        this.guildId = guild.getIdLong();
        this.channelId = channel == null ? 0 : channel.getIdLong();
        this.channelType = channel == null ? ChannelType.UNKNOWN : channel.getType();
    }

    /**
     * The id for the responsible {@link lonter.jfa.api.entities.Guild} instance.
     *
     * @return The ID as a long
     *
     * @see    lonter.jfa.api.JFA#getGuildById(long)
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * The id for the responsible {@link GuildChannel} instance.
     *
     * @return The ID as a long or 0
     *
     * @see    #getChannel(lonter.jfa.api.JFA)
     */
    public long getChannelId() {
        return channelId;
    }

    /**
     * The {@link ChannelType} for the {@link #getChannelId() channel id}.
     *
     * @return The channel type or {@link ChannelType#UNKNOWN}.
     */
    @NotNull
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * The {@link lonter.jfa.api.entities.Guild} instance for the {@link #getGuildId() guild id}.
     *
     * @param  api
     *         The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided JFA instance is null
     *
     * @return The Guild instance or null
     */
    @Nullable
    public Guild getGuild(@NotNull JFA api) {
        Checks.notNull(api, "JFA");
        return api.getGuildById(guildId);
    }

    /**
     * The {@link GuildChannel} instance for the {@link #getChannelId() channel id}.
     *
     * @param  api
     *         The shard to perform the lookup in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided JFA instance is null
     *
     * @return The GuildChannel instance or null
     */
    @Nullable
    public GuildChannel getChannel(@NotNull JFA api) {
        Checks.notNull(api, "JFA");
        return api.getGuildChannelById(channelType, channelId);
    }
}
