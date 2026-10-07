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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.requests.restaction.ChannelAction;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a GuildChannel that is capable of being copied.
 *
 * <p>Please see {@link ICopyableChannel#createCopy()} for information on what is copied.
 */
public interface ICopyableChannel extends GuildChannel {
    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}
     * in the specified {@link lonter.jfa.api.entities.Guild Guild}.
     * <br>If the provided target guild is not the same Guild this channel is in then
     * the parent category and permissions will not be copied due to technical difficulty and ambiguity.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link lonter.jfa.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link lonter.jfa.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @param  guild
     *         The {@link lonter.jfa.api.entities.Guild Guild} to create the channel in
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided guild is {@code null}
     * @throws lonter.jfa.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If the guild the channel should be copied to is {@link Guild#isDetached() detached}
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<? extends ICopyableChannel> createCopy(@NotNull Guild guild);

    /**
     * Creates a copy of the specified {@link GuildChannel GuildChannel}.
     *
     * <p>This copies the following elements:
     * <ol>
     *     <li>Name</li>
     *     <li>Parent Category (if present)</li>
     *     <li>Voice Elements (Bitrate, Userlimit)</li>
     *     <li>Text Elements (Topic, NSFW, Slowmode)</li>
     *     <li>All permission overrides for Members/Roles</li>
     * </ol>
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link lonter.jfa.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The channel could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link lonter.jfa.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.PermissionException
     *         If the currently logged in account does not have the {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} Permission
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return A specific {@link ChannelAction ChannelAction}
     *         <br>This action allows to set fields for the new GuildChannel before creating it!
     */
    @NotNull
    @CheckReturnValue
    ChannelAction<? extends ICopyableChannel> createCopy();
}
