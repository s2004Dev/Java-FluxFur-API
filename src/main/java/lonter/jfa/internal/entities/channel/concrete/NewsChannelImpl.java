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

package lonter.jfa.internal.entities.channel.concrete;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.NewsChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.managers.channel.concrete.NewsChannelManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.concrete.NewsChannelMixin;
import lonter.jfa.internal.managers.channel.concrete.NewsChannelManagerImpl;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.Helpers;

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
