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

package lonter.jfa.internal.entities.channel.concrete.detached;

import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.NewsChannel;
import lonter.jfa.api.entities.channel.unions.DefaultGuildChannelUnion;
import lonter.jfa.api.managers.channel.concrete.NewsChannelManager;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.internal.entities.channel.middleman.AbstractStandardGuildMessageChannelImpl;
import lonter.jfa.internal.entities.channel.mixin.attribute.IInteractionPermissionMixin;
import lonter.jfa.internal.entities.channel.mixin.concrete.NewsChannelMixin;
import lonter.jfa.internal.entities.detached.DetachedGuildImpl;
import lonter.jfa.internal.interactions.ChannelInteractionPermissions;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class DetachedNewsChannelImpl extends AbstractStandardGuildMessageChannelImpl<DetachedNewsChannelImpl>
        implements NewsChannel,
                DefaultGuildChannelUnion,
                NewsChannelMixin<DetachedNewsChannelImpl>,
                IInteractionPermissionMixin<DetachedNewsChannelImpl> {
    private ChannelInteractionPermissions interactionPermissions;

    public DetachedNewsChannelImpl(long id, DetachedGuildImpl guild) {
        super(id, guild);
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @NotNull
    @Override
    public ChannelType getType() {
        return ChannelType.NEWS;
    }

    @NotNull
    @Override
    public List<Member> getMembers() {
        throw detachedException();
    }

    @NotNull
    @Override
    public RestAction<Webhook.WebhookReference> follow(@NotNull String targetChannelId) {
        throw detachedException();
    }

    @NotNull
    @Override
    public NewsChannelManager getManager() {
        throw detachedException();
    }

    @NotNull
    @Override
    public ChannelInteractionPermissions getInteractionPermissions() {
        return interactionPermissions;
    }

    @NotNull
    @Override
    public DetachedNewsChannelImpl setInteractionPermissions(
            @NotNull ChannelInteractionPermissions interactionPermissions) {
        this.interactionPermissions = interactionPermissions;
        return this;
    }
}
