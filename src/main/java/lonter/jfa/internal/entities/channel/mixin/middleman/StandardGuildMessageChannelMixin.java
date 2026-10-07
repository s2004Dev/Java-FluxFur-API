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

package lonter.jfa.internal.entities.channel.mixin.middleman;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.channel.middleman.StandardGuildMessageChannel;
import lonter.jfa.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IThreadContainerMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.ITopicChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;

import org.jetbrains.annotations.NotNull;

public interface StandardGuildMessageChannelMixin<T extends StandardGuildMessageChannelMixin<T>>
        extends StandardGuildMessageChannel,
                StandardGuildChannelMixin<T>,
                GuildMessageChannelMixin<T>,
                IThreadContainerMixin<T>,
                IAgeRestrictedChannelMixin<T>,
                IWebhookContainerMixin<T>,
                ITopicChannelMixin<T> {
    // ---- Default implementations of interface ----
    @Override
    default boolean canTalk(@NotNull Member member) {
        if (!getGuild().equals(member.getGuild())) {
            throw new IllegalArgumentException("Provided Member is not from the Guild that this channel is part of.");
        }

        return member.hasPermission(this, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
    }
}
