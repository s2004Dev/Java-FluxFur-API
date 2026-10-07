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

package lonter.jfa.api.managers;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.unions.IPermissionContainerUnion;
import lonter.jfa.internal.utils.Checks;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Manager providing functionality to update one or more fields for a {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
 *
 * <p><b>Example</b>
 * {@snippet lang="java":
 * manager.setDenied(Permission.MESSAGE_SEND)
 *        .setAllowed(Permission.VIEW_CHANNEL)
 *        .queue();
 * manager.reset(PermOverrideManager.DENIED | PermOverrideManager.ALLOWED)
 *        .grant(Permission.MESSAGE_SEND)
 *        .clear(Permission.MESSAGE_MANAGE)
 *        .queue();
 * }
 *
 * @see lonter.jfa.api.entities.PermissionOverride#getManager()
 */
public interface PermOverrideManager extends Manager<PermOverrideManager> {
    /** Used to reset the denied field */
    long DENIED = 1;
    /** Used to reset the granted field */
    long ALLOWED = 1 << 1;
    /** Used to reset <b>all</b> permissions to their original value */
    long PERMISSIONS = ALLOWED | DENIED;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(PermOverrideManager.ALLOWED | PermOverrideManager.DENIED);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DENIED}</li>
     *     <li>{@link #ALLOWED}</li>
     *     <li>{@link #PERMISSIONS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    PermOverrideManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(PermOverrideManager.ALLOWED, PermOverrideManager.DENIED);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DENIED}</li>
     *     <li>{@link #ALLOWED}</li>
     *     <li>{@link #PERMISSIONS}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    PermOverrideManager reset(@NotNull long... fields);

    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} this Manager's
     * {@link GuildChannel GuildChannel} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getGuild()}
     *
     * @return The parent {@link lonter.jfa.api.entities.Guild Guild}
     */
    @NotNull
    default Guild getGuild() {
        return getPermissionOverride().getGuild();
    }

    /**
     * The {@link IPermissionContainer GuildChannel} this Manager's
     * {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride} is in.
     * <br>This is logically the same as calling {@code getPermissionOverride().getChannel()}
     *
     * @return The parent {@link GuildChannel GuildChannel}
     */
    @NotNull
    default IPermissionContainerUnion getChannel() {
        return getPermissionOverride().getChannel();
    }

    /**
     * The target {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     * that will be modified by this Manager
     *
     * @return The target {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     */
    @NotNull
    PermissionOverride getPermissionOverride();

    /**
     * Grants the provided {@link lonter.jfa.api.Permission Permissions} bits
     * to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    PermOverrideManager grant(long permissions);

    /**
     * Grants the provided {@link lonter.jfa.api.Permission Permissions}
     * to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...) Permission.getRaw(Permission...)
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager grant(@NotNull Permission... permissions) {
        Checks.notNull(permissions, "Permissions");
        return grant(Permission.getRaw(permissions));
    }

    /**
     * Grants the provided {@link lonter.jfa.api.Permission Permissions}
     * to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to grant to the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager grant(@NotNull Collection<Permission> permissions) {
        return grant(Permission.getRaw(permissions));
    }

    /**
     * Denies the provided {@link lonter.jfa.api.Permission Permissions} bits
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    PermOverrideManager deny(long permissions);

    /**
     * Denies the provided {@link lonter.jfa.api.Permission Permissions}
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...) Permission.getRaw(Permission...)
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager deny(@NotNull Permission... permissions) {
        Checks.notNull(permissions, "Permissions");
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Denies the provided {@link lonter.jfa.api.Permission Permissions}
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     *
     * @param  permissions
     *         The permissions to deny from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager deny(@NotNull Collection<Permission> permissions) {
        return deny(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link lonter.jfa.api.Permission Permissions} bits
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    PermOverrideManager clear(long permissions);

    /**
     * Clears the provided {@link lonter.jfa.api.Permission Permissions} bits
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager clear(@NotNull Permission... permissions) {
        Checks.notNull(permissions, "Permissions");
        return clear(Permission.getRaw(permissions));
    }

    /**
     * Clears the provided {@link lonter.jfa.api.Permission Permissions} bits
     * from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}.
     * <br>This will cause the provided Permissions to be inherited
     *
     * @param  permissions
     *         The permissions to clear from the selected {@link lonter.jfa.api.entities.PermissionOverride PermissionOverride}
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return PermOverrideManager for chaining convenience
     *
     * @see    java.util.EnumSet EnumSet
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     */
    @NotNull
    @CheckReturnValue
    default PermOverrideManager clear(@NotNull Collection<Permission> permissions) {
        return clear(Permission.getRaw(permissions));
    }
}
