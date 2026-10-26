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

package it.lonter.jfa.internal.entities.channel.concrete;

import it.lonter.jfa.api.Permission;
import it.lonter.jfa.api.entities.Member;
import it.lonter.jfa.api.entities.Webhook;
import it.lonter.jfa.api.entities.channel.ChannelType;
import it.lonter.jfa.api.entities.channel.concrete.NewsChannel;
import it.lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import it.lonter.jfa.api.managers.channel.concrete.NewsChannelManager;
import it.lonter.jfa.api.requests.RestAction;
import it.lonter.jfa.api.requests.Route;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.entities.GuildImpl;
import it.lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import it.lonter.jfa.internal.entities.channel.mixin.concrete.NewsChannelMixin;
import it.lonter.jfa.internal.managers.channel.concrete.NewsChannelManagerImpl;
import it.lonter.jfa.internal.requests.RestActionImpl;
import it.lonter.jfa.internal.utils.Checks;
import it.lonter.jfa.internal.utils.Helpers;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class NewsChannelImpl extends AbstractStandardGuildMessageChannelImpl<NewsChannelImpl>
        implements NewsChannel, DefaultGuildChannelUnion, NewsChannelMixin<NewsChannelImpl> {
    public NewsChannelImpl(long id, GuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    @NotNull
    @Override
    public GuildImpl getGuild() {
        return (GuildImpl) super.getGuild();
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.NEWS;
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        return getGuild().getMembersView().stream()
                .filter(m -> m.hasPermission(this, Permission.VIEW_CHANNEL))
                .collect(Helpers.toUnmodifiableList());
    }

    @NotNull
    @Override
    public RestAction<Webhook.WebhookReference> follow(@NotNull String targetChannelId) {
        Checks.notNull(targetChannelId, "Target Channel ID");

        Route.CompiledRoute route = Route.Channels.FOLLOW_CHANNEL.compile(getId());
        DataObject body = DataObject.empty().put("webhook_channel_id", targetChannelId);
        return new RestActionImpl<>(getJFA(), route, body, (response, request) -> {
            DataObject json = response.getObject();
            return new Webhook.WebhookReference(
                    request.getJFA(), json.getUnsignedLong("webhook_id"), json.getUnsignedLong("channel_id"));
        });
    }

    @NotNull
    @Override
    public NewsChannelManager getManager() {
        return new NewsChannelManagerImpl(this);
    }
}
