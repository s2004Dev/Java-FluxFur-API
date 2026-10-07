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

package lonter.jfa.api.entities.channel.middleman;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.Channel;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.managers.channel.ChannelManager;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.internal.utils.Helpers;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link lonter.jfa.api.entities.Guild Guild} channel.
 *
 * @see Guild#getGuildChannelById(long)
 * @see Guild#getGuildChannelById(ChannelType, long)
 * @see JFA#getGuildChannelById(long)
 * @see JFA#getGuildChannelById(ChannelType, long)
 */
public interface GuildChannel extends Channel, Comparable<GuildChannel> {
    /** Template for {@link #getJumpUrl()}.*/
    String JUMP_URL = "https://fluxer.com/channels/%s/%s";

    /**
     * Returns the {@link lonter.jfa.api.entities.Guild Guild} that this GuildChannel is part of.
     *
     * @return Never-null {@link lonter.jfa.api.entities.Guild Guild} that this GuildChannel is part of.
     */
    @NotNull
    Guild getGuild();

    /**
     * Returns the {@link ChannelManager ChannelManager} for this GuildChannel.
     * <br>In the ChannelManager, you can modify the name, topic and position of this GuildChannel.
     * You modify multiple fields in one request by chaining setters before calling {@link lonter.jfa.api.requests.RestAction#queue() RestAction.queue()}.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link lonter.jfa.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return The ChannelManager of this GuildChannel
     */
    @NotNull
    @CheckReturnValue
    ChannelManager<?, ?> getManager();

    /**
     * Deletes this GuildChannel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>If this channel was already deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MANAGE_CHANNEL Permission.MANAGE_CHANNEL} in the channel.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>If we were removed from the Guild</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         if the currently logged in account doesn't have {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL}
     *         for the channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @Override
    @NotNull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * The channel containing the permissions relevant to this channel.
     *
     * <p>This is usually the same channel, but for threads the parent channel is used instead.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return The permission container
     */
    @NotNull
    IPermissionContainer getPermissionContainer();

    /**
     * Returns the jump-to URL for this channel. Clicking this URL in the Fluxer client will cause the client to
     * jump to the specified channel.
     *
     * @return A String representing the jump-to URL for the channel.
     */
    @NotNull
    default String getJumpUrl() {
        return Helpers.format(JUMP_URL, getGuild().getId(), getId());
    }
}
