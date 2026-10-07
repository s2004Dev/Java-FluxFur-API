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

package lonter.jfa.api.entities.channel.attribute;

import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.managers.channel.attribute.IPermissionContainerManager;
import lonter.jfa.api.requests.restaction.PermissionOverrideAction;
import lonter.jfa.internal.utils.Helpers;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link GuildChannel} that uses {@link lonter.jfa.api.entities.PermissionOverride Permission Overrides}.
 *
 * <p>Channels that implement this interface can override permissions for specific users or roles.
 *
 * @see lonter.jfa.api.entities.PermissionOverride
 */
public interface IPermissionContainer extends GuildChannel {
    @Override
    @NotNull
    @CheckReturnValue
    IPermissionContainerManager<?, ?> getManager();

    /**
     * The {@link lonter.jfa.api.entities.PermissionOverride} relating to the specified {@link lonter.jfa.api.entities.Member Member} or {@link lonter.jfa.api.entities.Role Role}.
     * If there is no {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} for this {@link GuildChannel GuildChannel}
     * relating to the provided Member or Role, then this returns {@code null}.
     *
     * @param  permissionHolder
     *         The {@link lonter.jfa.api.entities.Member Member} or {@link lonter.jfa.api.entities.Role Role} whose
     *         {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} is requested.
     *
     * @throws IllegalArgumentException
     *         If the provided permission holder is null, or from a different guild
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return Possibly-null {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *         relating to the provided Member or Role.
     */
    @Nullable
    PermissionOverride getPermissionOverride(@NotNull IPermissionHolder permissionHolder);

    /**
     * Gets all of the {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides} that are part
     * of this {@link GuildChannel GuildChannel}.
     * <br>This combines {@link lonter.jfa.api.entities.Member Member} and {@link lonter.jfa.api.entities.Role Role} overrides.
     * If you would like only {@link lonter.jfa.api.entities.Member Member} overrides or only {@link lonter.jfa.api.entities.Role Role}
     * overrides, use {@link #getMemberPermissionOverrides()} or {@link #getRolePermissionOverrides()} respectively.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled!
     * Without that CacheFlag, this list will only contain overrides for the currently logged in account and roles.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return Possibly-empty immutable list of all {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @NotNull
    @Unmodifiable
    List<PermissionOverride> getPermissionOverrides();

    /**
     * Gets all of the {@link lonter.jfa.api.entities.Member Member} {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * <p>This requires {@link lonter.jfa.api.utils.cache.CacheFlag#MEMBER_OVERRIDES CacheFlag.MEMBER_OVERRIDES} to be enabled!
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return Possibly-empty immutable list of all {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link lonter.jfa.api.entities.Member Member}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @NotNull
    @Unmodifiable
    default List<PermissionOverride> getMemberPermissionOverrides() {
        return getPermissionOverrides().stream()
                .filter(PermissionOverride::isMemberOverride)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Gets all of the {@link lonter.jfa.api.entities.Role Role} {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides}
     * that are part of this {@link GuildChannel GuildChannel}.
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return Possibly-empty immutable list of all {@link lonter.jfa.api.entities.PermissionOverride PermissionOverrides}
     *         for {@link lonter.jfa.api.entities.Role Roles}
     *         for this {@link GuildChannel GuildChannel}.
     */
    @NotNull
    @Unmodifiable
    default List<PermissionOverride> getRolePermissionOverrides() {
        return getPermissionOverrides().stream()
                .filter(PermissionOverride::isRoleOverride)
                .collect(Helpers.toUnmodifiableList());
    }

    /**
     * Creates a new override or updates an existing one.
     * <br>This is similar to calling {@link PermissionOverride#getManager()} if an override exists.
     *
     * @param  permissionHolder
     *         The Member/Role for the override
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If we don't have the permission to {@link lonter.jfa.api.Permission#MANAGE_PERMISSIONS MANAGE_PERMISSIONS}
     * @throws java.lang.IllegalArgumentException
     *         If the provided permission holder is null or not from this guild
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.PermissionOverrideAction}
     *         <br>With the current settings of an existing override or a fresh override with no permissions set
     *
     * @see    PermissionOverrideAction#clear(long)
     * @see    PermissionOverrideAction#grant(long)
     * @see    PermissionOverrideAction#deny(long)
     */
    @NotNull
    @CheckReturnValue
    PermissionOverrideAction upsertPermissionOverride(@NotNull IPermissionHolder permissionHolder);
}
