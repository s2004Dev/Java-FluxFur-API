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

package lonter.jfa.internal.requests;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.IncomingWebhookClient;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.WebhookMessageDeleteAction;
import lonter.jfa.api.requests.restaction.WebhookMessageRetrieveAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.AbstractWebhookClient;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.ReceivedMessage;
import lonter.jfa.internal.requests.restaction.WebhookMessageCreateActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageDeleteActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageEditActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageRetrieveActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

public class IncomingWebhookClientImpl extends AbstractWebhookClient<Message> implements IncomingWebhookClient {
    public IncomingWebhookClientImpl(long webhookId, String webhookToken, JFA api) {
        super(webhookId, webhookToken, api);
    }

    @Override
    public WebhookMessageCreateActionImpl<Message> sendRequest() {
        Route.CompiledRoute route = Route.Webhooks.EXECUTE_WEBHOOK.compile(Long.toUnsignedString(id), token);
        route = route.withQueryParams("wait", "true");
        route = route.withQueryParams("with_components", "true");
        WebhookMessageCreateActionImpl<Message> action = new WebhookMessageCreateActionImpl<>(api, route, builder());
        action.run();
        action.setInteraction(false);
        return action;
    }

    @Override
    public WebhookMessageEditActionImpl<Message> editRequest(@NotNull String messageId) {
        if (!"@original".equals(messageId)) {
            Checks.isSnowflake(messageId);
        }
        Route.CompiledRoute route =
                Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(Long.toUnsignedString(id), token, messageId);
        route = route.withQueryParams("wait", "true");
        route = route.withQueryParams("with_components", "true");
        WebhookMessageEditActionImpl<Message> action = new WebhookMessageEditActionImpl<>(api, route, builder());
        action.run();
        return action;
    }

    @NotNull
    @Override
    public WebhookMessageRetrieveAction retrieveMessageById(@NotNull String messageId) {
        if (!"@original".equals(messageId)) {
            Checks.isSnowflake(messageId);
        }
        Route.CompiledRoute route =
                Route.Webhooks.EXECUTE_WEBHOOK_FETCH.compile(Long.toUnsignedString(id), token, messageId);
        WebhookMessageRetrieveActionImpl action = new WebhookMessageRetrieveActionImpl(
                api, route, (response, request) -> builder().apply(response.getObject()));
        action.run();
        return action;
    }

    @NotNull
    @Override
    public WebhookMessageDeleteAction deleteMessageById(@NotNull String messageId) {
        WebhookMessageDeleteActionImpl action = (WebhookMessageDeleteActionImpl) super.deleteMessageById(messageId);
        action.run();
        return action;
    }

    private Function<DataObject, Message> builder() {
        return (data) -> {
            JFAImpl jfa = (JFAImpl) api;
            long channelId = data.getUnsignedLong("channel_id");
            MessageChannel channel = api.getChannelById(MessageChannel.class, channelId);
            EntityBuilder entityBuilder = jfa.getEntityBuilder();
            ReceivedMessage message = entityBuilder.createMessageBestEffort(data, channel, null);
            message.withHook(this);
            return message;
        };
    }
}
