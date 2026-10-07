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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.concrete.NewsChannel;
import lonter.jfa.api.requests.restaction.ChannelAction;
import lonter.jfa.internal.entities.channel.mixin.middleman.StandardGuildMessageChannelMixin;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public interface NewsChannelMixin<T extends NewsChannelMixin<T>>
        extends NewsChannel, StandardGuildMessageChannelMixin<T> {
    @NotNull
    @Override
    default ChannelAction<NewsChannel> createCopy(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");
        ChannelAction<NewsChannel> action =
                guild.createNewsChannel(getName()).setNSFW(isNSFW()).setTopic(getTopic());
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
