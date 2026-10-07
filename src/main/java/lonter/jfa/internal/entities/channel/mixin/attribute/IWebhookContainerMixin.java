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

package lonter.jfa.internal.entities.channel.mixin.attribute;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.attribute.IWebhookContainer;
import lonter.jfa.api.entities.channel.unions.IWebhookContainerUnion;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.WebhookAction;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface IWebhookContainerMixin<T extends IWebhookContainerMixin<T>>
        extends IWebhookContainer, IWebhookContainerUnion, GuildChannelMixin<T> {
    // ---- Default implementations of interface ----
    @NotNull
    @Override
    default RestAction<List<Webhook>> retrieveWebhooks() {
        checkAttached();
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Channels.GET_WEBHOOKS.compile(getId());
        JFAImpl jfa = (JFAImpl) getJFA();
        return new RestActionImpl<>(jfa, route, (response, request) -> {
            DataArray array = response.getArray();
            List<Webhook> webhooks = new ArrayList<>(array.length());
            EntityBuilder builder = jfa.getEntityBuilder();

            for (int i = 0; i < array.length(); i++) {
                try {
                    webhooks.add(builder.createWebhook(array.getObject(i)));
                } catch (UncheckedIOException | NullPointerException e) {
                    JFAImpl.LOG.error("Error while creating websocket from json", e);
                }
            }

            return Collections.unmodifiableList(webhooks);
        });
    }

    @NotNull
    @Override
    default WebhookAction createWebhook(@NotNull String name) {
        Checks.notBlank(name, "Webhook name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkAttached();
        checkPermission(Permission.MANAGE_WEBHOOKS);

        return new WebhookActionImpl(getJFA(), this, name);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> deleteWebhookById(@NotNull String id) {
        Checks.isSnowflake(id, "Webhook ID");

        checkAttached();
        checkPermission(Permission.MANAGE_WEBHOOKS);

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(id);
        return new AuditableRestActionImpl<>(getJFA(), route);
    }
}
