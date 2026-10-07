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

package lonter.jfa.internal.entities.channel.mixin.attribute;

import gnu.trove.map.TLongObjectMap;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.unions.IPermissionContainerUnion;
import lonter.jfa.api.requests.restaction.PermissionOverrideAction;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import lonter.jfa.internal.requests.restaction.PermissionOverrideActionImpl;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface IPermissionContainerMixin<T extends IPermissionContainerMixin<T>>
        extends IPermissionContainer, IPermissionContainerUnion, GuildChannelMixin<T> {
    // ---- Default implementations of interface ----
    @Override
    default PermissionOverride getPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        Checks.notNull(permissionHolder, "Permission Holder");
        Checks.check(
                permissionHolder.getGuild().equals(getGuild()),
                "Provided permission holder is not from the same guild as this channel!");

        TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
        return overrides.get(permissionHolder.getIdLong());
    }

    @NotNull
    @Override
    default List<PermissionOverride> getPermissionOverrides() {
        TLongObjectMap<PermissionOverride> overrides = getPermissionOverrideMap();
        return Arrays.asList(overrides.values(new PermissionOverride[overrides.size()]));
    }

    @NotNull
    @Override
    default PermissionOverrideAction upsertPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        checkAttached();
        checkPermission(Permission.MANAGE_PERMISSIONS);
        Checks.notNull(permissionHolder, "PermissionHolder");
        Checks.check(
                permissionHolder.getGuild().equals(getGuild()),
                "Provided permission holder is not from the same guild as this channel!");

        PermissionOverride override = getPermissionOverride(permissionHolder);
        if (override != null) {
            return override.getManager();
        }
        return new PermissionOverrideActionImpl(getJFA(), this, permissionHolder);
    }

    // --- Default implementation of parent mixins hooks ----
    @Override
    @NotNull
    default IPermissionContainer getPermissionContainer() {
        return this;
    }

    // ---- State Accessors ----
    TLongObjectMap<PermissionOverride> getPermissionOverrideMap();
}
