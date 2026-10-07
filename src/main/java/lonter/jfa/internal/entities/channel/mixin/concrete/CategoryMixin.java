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
import lonter.jfa.api.requests.restaction.ChannelAction;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPermissionContainerMixin;
import lonter.jfa.internal.entities.channel.mixin.attribute.IPositionableChannelMixin;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public interface CategoryMixin<T extends CategoryMixin<T>>
        extends Category, IPositionableChannelMixin<T>, IPermissionContainerMixin<T> {
    @NotNull
    @Override
    default ChannelAction<Category> createCopy(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");
        ChannelAction<Category> action = guild.createCategory(getName());
        if (guild.equals(getGuild())) {
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
