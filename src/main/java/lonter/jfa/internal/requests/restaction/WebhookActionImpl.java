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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.attribute.IWebhookContainer;
import lonter.jfa.api.entities.channel.unions.IWebhookContainerUnion;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.WebhookAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * {@link lonter.jfa.api.entities.Webhook Webhook} Builder system created as an extension of {@link lonter.jfa.api.requests.RestAction}
 * <br>Provides an easy way to gather and deliver information to Fluxer to create {@link lonter.jfa.api.entities.Webhook Webhooks}.
 */
public class WebhookActionImpl extends AuditableRestActionImpl<Webhook> implements WebhookAction {
    protected final IWebhookContainer channel;
    protected String name;
    protected Icon avatar = null;

    public WebhookActionImpl(JFA api, IWebhookContainer channel, String name) {
        super(api, Route.Channels.CREATE_WEBHOOK.compile(channel.getId()));
        this.channel = channel;
        this.name = name;
    }

    @NotNull
    @Override
    public WebhookActionImpl setCheck(BooleanSupplier checks) {
        return (WebhookActionImpl) super.setCheck(checks);
    }

    @NotNull
    @Override
    public WebhookActionImpl timeout(long timeout, @NotNull TimeUnit unit) {
        return (WebhookActionImpl) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public WebhookActionImpl deadline(long timestamp) {
        return (WebhookActionImpl) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public IWebhookContainerUnion getChannel() {
        return (IWebhookContainerUnion) channel;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookActionImpl setName(@NotNull String name) {
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        this.name = name;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookActionImpl setAvatar(Icon icon) {
        this.avatar = icon;
        return this;
    }

    @Override
    public RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        object.put("name", name);
        object.put("avatar", avatar != null ? avatar.getEncoding() : null);

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<Webhook> request) {
        DataObject json = response.getObject();
        Webhook webhook = api.getEntityBuilder().createWebhook(json);

        request.onSuccess(webhook);
    }
}
