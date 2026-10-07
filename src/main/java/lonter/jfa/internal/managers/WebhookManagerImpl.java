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

package lonter.jfa.internal.managers;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.WebhookManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class WebhookManagerImpl extends ManagerBase<WebhookManager> implements WebhookManager {
    protected final Webhook webhook;
    protected String name;
    protected String channel;
    protected Icon avatar;

    /**
     * Creates a new WebhookManager instance
     *
     * @param webhook
     *        The target {@link lonter.jfa.api.entities.Webhook Webhook} to modify
     */
    public WebhookManagerImpl(Webhook webhook) {
        super(webhook.getJFA(), Route.Webhooks.MODIFY_WEBHOOK.compile(webhook.getId()));
        this.webhook = webhook;
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    @NotNull
    @Override
    public Webhook getWebhook() {
        return webhook;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        if ((fields & CHANNEL) == CHANNEL) {
            this.channel = null;
        }
        if ((fields & AVATAR) == AVATAR) {
            this.avatar = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl reset() {
        super.reset();
        this.name = null;
        this.channel = null;
        this.avatar = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setName(@NotNull String name) {
        Checks.notBlank(name, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setAvatar(Icon icon) {
        this.avatar = icon;
        set |= AVATAR;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public WebhookManagerImpl setChannel(@NotNull TextChannel channel) {
        Checks.notNull(channel, "Channel");
        Checks.check(channel.getGuild().equals(getGuild()), "Channel is not from the same guild");
        this.channel = channel.getId();
        set |= CHANNEL;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject data = DataObject.empty();
        if (shouldUpdate(NAME)) {
            data.put("name", name);
        }
        if (shouldUpdate(CHANNEL)) {
            data.put("channel_id", channel);
        }
        if (shouldUpdate(AVATAR)) {
            data.put("avatar", avatar == null ? null : avatar.getEncoding());
        }

        return getRequestBody(data);
    }

    @Override
    protected boolean checkPermissions() {
        Member selfMember = getGuild().getSelfMember();
        GuildChannel guildChannel = getChannel();
        Checks.checkAccess(selfMember, guildChannel);
        if (!selfMember.hasPermission(guildChannel, Permission.MANAGE_WEBHOOKS)) {
            throw new InsufficientPermissionException(guildChannel, Permission.MANAGE_WEBHOOKS);
        }
        return super.checkPermissions();
    }
}
