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

package lonter.jfa.internal.entities.channel.mixin.concrete;

import lonter.jfa.api.Permission;
import lonter.jfa.api.Region;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.concrete.VoiceChannel;
import lonter.jfa.api.requests.restaction.ChannelAction;
import lonter.jfa.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;
import lonter.jfa.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public interface VoiceChannelMixin<T extends VoiceChannelMixin<T>>
        extends VoiceChannel,
                GuildMessageChannelMixin<T>,
                AudioChannelMixin<T>,
                IWebhookContainerMixin<T>,
                IAgeRestrictedChannelMixin<T>,
                ISlowmodeChannelMixin<T> {
    @Override
    default boolean canTalk(@NotNull Member member) {
        Checks.notNull(member, "Member");
        return member.hasPermission(this, Permission.MESSAGE_SEND);
    }

    @NotNull
    @Override
    default ChannelAction<VoiceChannel> createCopy(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");

        ChannelAction<VoiceChannel> action =
                guild.createVoiceChannel(getName()).setBitrate(getBitrate()).setUserlimit(getUserLimit());

        if (getRegionRaw() != null) {
            action.setRegion(Region.fromKey(getRegionRaw()));
        }

        if (guild.equals(getGuild())) {
            Category parent = getParentCategory();
            if (parent != null) {
                action.setParent(parent);
            }
            for (PermissionOverride o : getPermissionOverrideMap().valueCollection()) {
                if (o.isMemberOverride()) {
                    action.addMemberPermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                } else {
                    action.addRolePermissionOverride(o.getIdLong(), o.getAllowedRaw(), o.getDeniedRaw());
                }
            }
        }
        return action;
    }

    T setStatus(String status);
}
