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
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.WebhookClient;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.interactions.InteractionHook;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.MessageEditAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.messages.MessageEditBuilder;
import lonter.jfa.api.utils.messages.MessageEditData;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.ReceivedMessage;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.message.MessageEditBuilderMixin;
import okhttp3.RequestBody;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageEditActionImpl extends RestActionImpl<Message>
        implements MessageEditAction, MessageEditBuilderMixin<MessageEditAction> {
    private final String messageId;
    private final Guild guild;
    private final MessageChannel channel;
    private final MessageEditBuilder builder = new MessageEditBuilder();
    private WebhookClient<Message> webhook;
    private String threadId;

    public MessageEditActionImpl(
            @NotNull JFA jfa, @Nullable Guild guild, @NotNull String channelId, @NotNull String messageId) {
        super(jfa, Route.Messages.EDIT_MESSAGE.compile(channelId, messageId));
        this.channel = null;
        this.guild = guild;
        this.messageId = messageId;
    }

    public MessageEditActionImpl(@NotNull MessageChannel channel, @NotNull String messageId) {
        super(channel.getJFA(), Route.Messages.EDIT_MESSAGE.compile(channel.getId(), messageId));
        this.channel = channel;
        this.guild = channel instanceof GuildChannel ? ((GuildChannel) channel).getGuild() : null;
        this.messageId = messageId;
    }

    public MessageEditActionImpl withHook(WebhookClient<Message> hook, ChannelType channelType, long channelId) {
        this.webhook = hook;
        if (!(hook instanceof InteractionHook) && channelType.isThread()) {
            this.threadId = Long.toUnsignedString(channelId);
        }
        return this;
    }

    @Override
    public MessageEditBuilder getBuilder() {
        return builder;
    }

    @Override
    protected Route.CompiledRoute finalizeRoute() {
        if (webhook != null && (!(webhook instanceof InteractionHook) || !((InteractionHook) webhook).isExpired())) {
            Route.CompiledRoute route =
                    Route.Webhooks.EXECUTE_WEBHOOK_EDIT.compile(webhook.getId(), webhook.getToken(), messageId);
            if (this.threadId != null) {
                route = route.withQueryParams("thread_id", threadId);
            }

            return route;
        }

        return super.finalizeRoute();
    }

    @Override
    protected RequestBody finalizeData() {
        try (MessageEditData data = builder.build()) {
            return getMultipartBody(data.getAllDistinctFiles(), data.toData());
        }
    }

    @Override
    protected void handleSuccess(Response response, Request<Message> request) {
        EntityBuilder entityBuilder = api.getEntityBuilder();
        DataObject json = response.getObject();
        ReceivedMessage message = entityBuilder.createMessageBestEffort(json, channel, guild);
        request.onSuccess(message.withHook(webhook));
    }

    @NotNull
    @Override
    public MessageEditAction setCheck(BooleanSupplier checks) {
        return (MessageEditAction) super.setCheck(checks);
    }

    @NotNull
    @Override
    public MessageEditAction deadline(long timestamp) {
        return (MessageEditAction) super.deadline(timestamp);
    }
}
