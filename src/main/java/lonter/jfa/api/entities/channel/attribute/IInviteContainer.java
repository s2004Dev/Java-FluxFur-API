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

import lonter.jfa.api.entities.Invite;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.InviteAction;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link GuildChannel GuildChannel} that can be the target of a Guild's invite.
 *
 * <p>Invites have to be targeted at exactly one {@link IInviteContainer}, which will open when the invite is used (unless restricted by permissions).
 */
public interface IInviteContainer extends GuildChannel {
    /**
     * Creates a new {@link InviteAction InviteAction} which can be used to create a
     * new {@link lonter.jfa.api.entities.Invite Invite}.
     * <br>Requires {@link lonter.jfa.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the account does not have {@link lonter.jfa.api.Permission#CREATE_INSTANT_INVITE CREATE_INSTANT_INVITE} in this channel
     * @throws java.lang.IllegalArgumentException
     *         If this is an instance of a {@link Category Category}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return A new {@link InviteAction InviteAction}
     *
     * @see    InviteAction
     */
    @NotNull
    @CheckReturnValue
    InviteAction createInvite();

    /**
     * Returns all invites for this channel.
     * <br>Requires {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel.
     * Will throw an {@link lonter.jfa.api.exceptions.InsufficientPermissionException InsufficientPermissionException} otherwise.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         if the account does not have {@link lonter.jfa.api.Permission#MANAGE_CHANNEL MANAGE_CHANNEL} in this channel
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: List{@literal <}{@link lonter.jfa.api.entities.Invite Invite}{@literal >}
     *         <br>The list of expanded Invite objects
     *
     * @see    lonter.jfa.api.entities.Guild#retrieveInvites()
     */
    @NotNull
    @CheckReturnValue
    RestAction<List<Invite>> retrieveInvites();
}
