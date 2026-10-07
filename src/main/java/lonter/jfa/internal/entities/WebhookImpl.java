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
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.*;
import lonter.jfa.api.entities.channel.attribute.IWebhookContainer;
import lonter.jfa.api.entities.channel.unions.IWebhookContainerUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.WebhookManager;
import lonter.jfa.api.requests.RestConfig;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.WebhookMessageDeleteAction;
import lonter.jfa.api.requests.restaction.WebhookMessageRetrieveAction;
import lonter.jfa.internal.managers.WebhookManagerImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageCreateActionImpl;
import lonter.jfa.internal.requests.restaction.WebhookMessageEditActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation for {@link lonter.jfa.api.entities.Webhook Webhook}
 */
public class WebhookImpl extends AbstractWebhookClient<Message> implements Webhook {
    private final IWebhookContainer channel;
    private final WebhookType type;

    private Member owner;
    private User user, ownerUser;
    private ChannelReference sourceChannel;
    private GuildReference sourceGuild;

    public WebhookImpl(IWebhookContainer channel, long id, WebhookType type) {
        this(channel, channel.getJFA(), id, type);
    }

    public WebhookImpl(IWebhookContainer channel, JFA api, long id, WebhookType type) {
        super(id, null, api);
        this.channel = channel;
        this.type = type;
    }

    @NotNull
    @Override
    public WebhookType getType() {
        return type;
    }

    @Override
    public boolean isPartial() {
        return channel == null;
    }

    @NotNull
    @Override
    public JFA getJFA() {
        return api;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        if (channel == null) {
            throw new IllegalStateException(
                    "Cannot provide guild for this Webhook instance because it does not belong to this shard");
        }
        return getChannel().getGuild();
    }

    @NotNull
    @Override
    public IWebhookContainerUnion getChannel() {
        if (channel == null) {
            throw new IllegalStateException(
                    "Cannot provide channel for this Webhook instance because it does not belong to this shard");
        }
        return (IWebhookContainerUnion) channel;
    }

    @Override
    public Member getOwner() {
        if (owner == null && channel != null && ownerUser != null) {
            return getGuild().getMember(ownerUser); // maybe it exists later?
        }
        return owner;
    }

    @Override
    public User getOwnerAsUser() {
        return ownerUser;
    }

    @NotNull
    @Override
    public User getDefaultUser() {
        return user;
    }

    @NotNull
    @Override
    public String getName() {
        return user.getName();
    }

    @NotNull
    @Override
    public String getUrl() {
        return RestConfig.DEFAULT_BASE_URL + "webhooks/" + getId() + (getToken() == null ? "" : "/" + getToken());
    }

    @Override
    public ChannelReference getSourceChannel() {
        return sourceChannel;
    }

    @Override
    public GuildReference getSourceGuild() {
        return sourceGuild;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        if (token != null) {
            return delete(token);
        }

        if (!getGuild().getSelfMember().hasPermission(getChannel(), Permission.MANAGE_WEBHOOKS)) {
            throw new InsufficientPermissionException(getChannel(), Permission.MANAGE_WEBHOOKS);
        }

        Route.CompiledRoute route = Route.Webhooks.DELETE_WEBHOOK.compile(getId());
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete(@NotNull String token) {
        Checks.notNull(token, "Token");
        Route.CompiledRoute route = Route.Webhooks.DELETE_TOKEN_WEBHOOK.compile(getId(), token);
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    public WebhookManager getManager() {
        return new WebhookManagerImpl(this);
    }

    // Webhook execution

    @Override
    public WebhookMessageCreateActionImpl<Message> sendRequest() {
        checkToken();
        AbstractWebhookClient<Message> client =
                (AbstractWebhookClient<Message>) WebhookClient.createClient(api, getId(), token);
        return client.sendRequest();
    }

    @Override
    public WebhookMessageEditActionImpl<Message> editRequest(String messageId) {
        checkToken();
        AbstractWebhookClient<Message> client =
                (AbstractWebhookClient<Message>) WebhookClient.createClient(api, getId(), token);
        return client.editRequest(messageId);
    }

    @NotNull
    @Override
    public WebhookMessageDeleteAction deleteMessageById(@NotNull String messageId) {
        checkToken();
        return WebhookClient.createClient(api, getId(), token).deleteMessageById(messageId);
    }

    @NotNull
    @Override
    public WebhookMessageRetrieveAction retrieveMessageById(@NotNull String messageId) {
        checkToken();
        return WebhookClient.createClient(api, getId(), token).retrieveMessageById(messageId);
    }

    private void checkToken() {
        if (token == null) {
            throw new UnsupportedOperationException("Cannot execute webhook without a token!");
        }
    }

    /* -- Impl Setters -- */

    public WebhookImpl setOwner(Member member, User user) {
        this.owner = member;
        this.ownerUser = user;
        return this;
    }

    public WebhookImpl setToken(String token) {
        this.token = token;
        return this;
    }

    public WebhookImpl setUser(User user) {
        this.user = user;
        return this;
    }

    public WebhookImpl setSourceGuild(GuildReference reference) {
        this.sourceGuild = reference;
        return this;
    }

    public WebhookImpl setSourceChannel(ChannelReference reference) {
        this.sourceChannel = reference;
        return this;
    }

    /* -- Object Overrides -- */

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WebhookImpl)) {
            return false;
        }
        WebhookImpl impl = (WebhookImpl) obj;
        return impl.id == id;
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(getName()).toString();
    }
}
