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
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.GuildChannelUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.exceptions.MissingAccessException;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.internal.entities.channel.mixin.ChannelMixin;
import lonter.jfa.internal.entities.detached.mixin.IDetachableEntityMixin;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface GuildChannelMixin<T extends GuildChannelMixin<T>>
        extends GuildChannel, GuildChannelUnion, ChannelMixin<T>, IDetachableEntityMixin {
    // ---- Default implementations of interface ----
    @Override
    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Void> delete() {
        checkCanAccess();
        checkCanManage();

        Route.CompiledRoute route = Route.Channels.DELETE_CHANNEL.compile(getId());
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    // ---- Helpers ---
    default boolean hasPermission(Permission permission) {
        IPermissionContainer permChannel = getPermissionContainer();
        return getGuild().getSelfMember().hasPermission(permChannel, permission);
    }

    default void checkPermission(Permission permission) {
        checkPermission(permission, null);
    }

    default void checkPermission(Permission permission, String message) {
        if (!hasPermission(permission)) {
            if (message != null) {
                throw new InsufficientPermissionException(this, permission, message);
            } else {
                throw new InsufficientPermissionException(this, permission);
            }
        }
    }

    // Overridden by ThreadChannelImpl
    default void checkCanManage() {
        checkPermission(Permission.MANAGE_CHANNEL);
    }

    // Overridden by AudioChannelMixin
    default void checkCanAccess() {
        checkAttached();
        if (!hasPermission(Permission.VIEW_CHANNEL)) {
            throw new MissingAccessException(this, Permission.VIEW_CHANNEL);
        }
    }
}
