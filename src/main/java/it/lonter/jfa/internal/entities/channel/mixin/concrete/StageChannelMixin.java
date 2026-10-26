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

package it.lonter.jfa.internal.entities.channel.mixin.concrete;

import it.lonter.jfa.api.Region;
import it.lonter.jfa.api.entities.Guild;
import it.lonter.jfa.api.entities.PermissionOverride;
import it.lonter.jfa.api.entities.channel.concrete.Category;
import it.lonter.jfa.api.entities.channel.concrete.StageChannel;
import it.lonter.jfa.api.requests.restaction.ChannelAction;
import it.lonter.jfa.internal.entities.channel.mixin.attribute.IAgeRestrictedChannelMixin;
import it.lonter.jfa.internal.entities.channel.mixin.attribute.ISlowmodeChannelMixin;
import it.lonter.jfa.internal.entities.channel.mixin.attribute.IWebhookContainerMixin;
import it.lonter.jfa.internal.entities.channel.mixin.middleman.AudioChannelMixin;
import it.lonter.jfa.internal.entities.channel.mixin.middleman.GuildMessageChannelMixin;
import it.lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public interface StageChannelMixin<T extends StageChannelMixin<T>>
        extends StageChannel,
                AudioChannelMixin<T>,
                GuildMessageChannelMixin<T>,
                IWebhookContainerMixin<T>,
                IAgeRestrictedChannelMixin<T>,
                ISlowmodeChannelMixin<T> {
    @NotNull
    @Override
    default ChannelAction<StageChannel> createCopy(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");

        ChannelAction<StageChannel> action = guild.createStageChannel(getName()).setBitrate(getBitrate());

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
}
