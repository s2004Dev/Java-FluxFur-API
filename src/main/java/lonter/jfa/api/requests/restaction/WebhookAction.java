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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.unions.IWebhookContainerUnion;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link lonter.jfa.api.entities.Webhook Webhook} Builder system created as an extension of {@link lonter.jfa.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Fluxer to create {@link lonter.jfa.api.entities.Webhook Webhooks}.
 *
 * @see TextChannel#createWebhook(String)
 */
public interface WebhookAction extends AuditableRestAction<Webhook> {
    @NotNull
    @Override
    @CheckReturnValue
    WebhookAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    WebhookAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    WebhookAction deadline(long timestamp);

    /**
     * The {@link lonter.jfa.api.entities.channel.attribute.IWebhookContainer channel} to create this webhook in.
     *
     * @return The channel
     */
    @NotNull
    IWebhookContainerUnion getChannel();

    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} to create this webhook in
     *
     * @return The guild
     */
    @NotNull
    default Guild getGuild() {
        return getChannel().getGuild();
    }

    /**
     * Sets the <b>Name</b> for the custom Webhook User
     *
     * @param  name
     *         A not-null String name for the new Webhook user.
     *
     * @throws IllegalArgumentException
     *         If the specified name is not in the range of 2-100.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @NotNull
    @CheckReturnValue
    WebhookAction setName(@NotNull String name);

    /**
     * Sets the <b>Avatar</b> for the custom Webhook User
     *
     * @param  icon
     *         An {@link lonter.jfa.api.entities.Icon Icon} for the new avatar.
     *         Or null to use default avatar.
     *
     * @return The current WebhookAction for chaining convenience.
     */
    @NotNull
    @CheckReturnValue
    WebhookAction setAvatar(@Nullable Icon icon);
}
