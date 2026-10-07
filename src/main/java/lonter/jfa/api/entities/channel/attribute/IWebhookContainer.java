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

import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.WebhookAction;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link GuildChannel} that is capable of utilizing <a href="https://support.fluxer.com/hc/en-us/articles/228383668-Intro-to-Webhooks" target="_blank">webhooks</a>.
 *
 * <p>Webhooks can be used to integrate third-party systems into Fluxer by way of sending information via messages.
 */
public interface IWebhookContainer extends GuildChannel {
    /**
     * Retrieves the {@link lonter.jfa.api.entities.Webhook Webhooks} attached to this channel.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in this channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.RestAction} - Type: List{@literal <}{@link lonter.jfa.api.entities.Webhook Webhook}{@literal >}
     *         <br>Retrieved an immutable list of Webhooks attached to this channel
     */
    @NotNull
    @CheckReturnValue
    RestAction<@Unmodifiable List<Webhook>> retrieveWebhooks();

    /**
     * Creates a new {@link lonter.jfa.api.entities.Webhook Webhook}.
     *
     * <p>Possible {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} caused by
     * the returned {@link lonter.jfa.api.requests.RestAction RestAction} include the following:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The webhook could not be created due to a permission discrepancy</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The {@link lonter.jfa.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission was removed</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MAX_WEBHOOKS MAX_WEBHOOKS}
     *     <br>If the channel already has reached the maximum capacity for webhooks</li>
     * </ul>
     *
     * @param  name
     *         The default name for the new Webhook.
     *
     * @throws lonter.jfa.api.exceptions.PermissionException
     *         If you do not hold the permission {@link lonter.jfa.api.Permission#MANAGE_WEBHOOKS Manage Webhooks}
     * @throws IllegalArgumentException
     *         If the provided name is {@code null}, blank or not
     *         between 2-100 characters in length
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return A specific {@link WebhookAction WebhookAction}
     *         <br>This action allows to set fields for the new webhook before creating it
     */
    @NotNull
    @CheckReturnValue
    WebhookAction createWebhook(@NotNull String name);

    /**
     * Deletes a {@link lonter.jfa.api.entities.Webhook Webhook} attached to this channel
     * by the {@code id} specified.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_WEBHOOK}
     *     <br>The provided id does not refer to a WebHook present in this channel, either due
     *         to it not existing or having already been deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in the channel.</li>
     * </ul>
     *
     * @param  id
     *         The not-null id for the target Webhook.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is {@code null} or empty.
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MANAGE_WEBHOOKS Permission.MANAGE_WEBHOOKS} in this channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @NotNull
    @CheckReturnValue
    AuditableRestAction<Void> deleteWebhookById(@NotNull String id);
}
