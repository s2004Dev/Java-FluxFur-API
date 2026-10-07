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

package lonter.jfa.internal.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.entities.WebhookClient;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.WebhookMessageCreateAction;
import lonter.jfa.api.requests.restaction.WebhookMessageDeleteAction;
import lonter.jfa.api.requests.restaction.WebhookMessageEditAction;
import lonter.jfa.api.utils.AttachedFile;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.messages.MessageCreateData;
import lonter.jfa.api.utils.messages.MessageEditData;
import lonter.jfa.api.utils.messages.MessagePollData;
import lonter.jfa.internal.requests.restaction.WebhookMessageCreateActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageDeleteActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageEditActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractWebhookClient<T> implements WebhookClient<T> {
    protected final long id;
    protected final JFA api;
    protected String token;

    protected AbstractWebhookClient(long webhookId, String webhookToken, JFA api) {
        this.id = webhookId;
        this.token = webhookToken;
        this.api = api;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public String getToken() {
        return token;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    public abstract WebhookMessageCreateActionImpl<T> sendRequest();

    public abstract WebhookMessageEditActionImpl<T> editRequest(String messageId);

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendMessage(@NotNull String content) {
        return sendRequest().setContent(content);
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        return sendRequest().addEmbeds(embeds);
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendMessageComponents(
            @NotNull Collection<? extends MessageTopLevelComponent> components) {
        return sendRequest().setComponents(components);
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendMessage(@NotNull MessageCreateData message) {
        return sendRequest().applyData(message);
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendMessagePoll(@NotNull MessagePollData poll) {
        Checks.notNull(poll, "Message Poll");
        return sendRequest().setPoll(poll);
    }

    @NotNull
    @Override
    public WebhookMessageCreateAction<T> sendFiles(@NotNull Collection<? extends FileUpload> files) {
        return sendRequest().addFiles(files);
    }

    @NotNull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageById(@NotNull String messageId, @NotNull String content) {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setContent(content);
    }

    @NotNull
    @Override
    public WebhookMessageEditAction<T> editMessageComponentsById(
            @NotNull String messageId, @NotNull Collection<? extends MessageTopLevelComponent> components) {
        Checks.noneNull(components, "Components");
        return editRequest(messageId).setComponents(components);
    }

    @NotNull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageEmbedsById(
            @NotNull String messageId, @NotNull Collection<? extends MessageEmbed> embeds) {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setEmbeds(embeds);
    }

    @NotNull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageById(
            @NotNull String messageId, @NotNull MessageEditData message) {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).applyData(message);
    }

    @NotNull
    @Override
    public WebhookMessageEditActionImpl<T> editMessageAttachmentsById(
            @NotNull String messageId, @NotNull Collection<? extends AttachedFile> attachments) {
        return (WebhookMessageEditActionImpl<T>) editRequest(messageId).setAttachments(attachments);
    }

    @NotNull
    @Override
    public WebhookMessageDeleteAction deleteMessageById(@NotNull String messageId) {
        if (!"@original".equals(messageId)) {
            Checks.isSnowflake(messageId);
        }
        Route.CompiledRoute route =
                Route.Webhooks.EXECUTE_WEBHOOK_DELETE.compile(Long.toUnsignedString(id), token, messageId);
        return new WebhookMessageDeleteActionImpl(api, route);
    }
}
