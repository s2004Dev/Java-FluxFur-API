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

package lonter.jfa.api.requests.restaction;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.RoleColors;
import lonter.jfa.api.entities.emoji.UnicodeEmoji;
import lonter.jfa.internal.utils.Checks;

import java.awt.*;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link lonter.jfa.api.requests.RestAction RestAction} specifically
 * designed to create a {@link lonter.jfa.api.entities.Role Role}.
 * This extension allows setting properties before executing the action.
 *
 * @see    lonter.jfa.api.entities.Guild
 * @see    lonter.jfa.api.entities.Guild#createRole()
 * @see    Role#createCopy()
 * @see    Role#createCopy(Guild)
 */
public interface RoleAction extends AuditableRestAction<Role> {
    @NotNull
    @Override
    @CheckReturnValue
    RoleAction setCheck(@Nullable BooleanSupplier checks);

    @NotNull
    @Override
    @CheckReturnValue
    RoleAction timeout(long timeout, @NotNull TimeUnit unit);

    @NotNull
    @Override
    @CheckReturnValue
    RoleAction deadline(long timestamp);

    /**
     * The guild to create the role in
     *
     * @return The guild
     */
    @NotNull
    Guild getGuild();

    /**
     * Sets the name for new role (optional)
     *
     * @param  name
     *         The name for the new role, null to use default name
     *
     * @throws java.lang.IllegalArgumentException
     *         If the provided name is longer than 100 characters
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setName(@Nullable String name);

    /**
     * Sets whether or not the new role should be hoisted
     *
     * @param  hoisted
     *         Whether the new role should be hoisted (grouped). Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setHoisted(@Nullable Boolean hoisted);

    /**
     * Sets whether the new role should be mentionable by members of
     * the parent {@link lonter.jfa.api.entities.Guild Guild}.
     *
     * @param  mentionable
     *         Whether the new role should be mentionable. Default is {@code false}
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setMentionable(@Nullable Boolean mentionable);

    /**
     * Sets the color which the new role should be displayed with.
     *
     * @param  color
     *         An {@link java.awt.Color Color} for the new role, null to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default RoleAction setColor(@Nullable Color color) {
        return this.setColor(color != null ? color.getRGB() : null);
    }

    /**
     * Sets the color for the new role.
     *
     * <p>This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rgb & 0xFFFFFF}
     *
     * @param  rgb
     *         The color for the new role in integer form, {@code null} to use default white/black
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setColor(@Nullable Integer rgb);

    /**
     * Sets the three color components of this role.
     *
     * <p>It is recommended to use {@link #setColor(Integer)}, {@link #setGradientColors(int, int)}, or {@link #useHolographicStyle()} for setting colors instead,
     * this method is primarily intended for copying colors from an existing role object with {@link Role#getColors()}.
     *
     * @param colors
     *        The role colors or {@code null} to use the default white/black
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see Role#getColors()
     */
    @NotNull
    @CheckReturnValue
    RoleAction setColors(@Nullable RoleColors colors);

    /**
     * Sets the primary and secondary color for the new role color gradient.
     *
     * <p>Use {@link #setColor(Color)} or {@link #useHolographicStyle()} to use a single color or holographic style instead.
     *
     * @param  primary
     *         The primary color for gradient
     * @param  secondary
     *         The secondary color for gradient
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    default RoleAction setGradientColors(@NotNull Color primary, @NotNull Color secondary) {
        Checks.notNull(primary, "Primary");
        Checks.notNull(secondary, "Secondary");
        return this.setGradientColors(primary.getRGB(), secondary.getRGB());
    }

    /**
     * Sets the primary and secondary color for the new role color gradient.
     *
     * <p>This accepts colors from the range {@code 0x000} to {@code 0xFFFFFF}.
     * The provided value will be ranged using {@code rgb & 0xFFFFFF}.
     *
     * <p>Use {@link #setColor(Integer)} or {@link #useHolographicStyle()} to use a single color or holographic style instead.
     *
     * @param  primaryRgb
     *         The primary color for gradient
     * @param  secondaryRgb
     *         The secondary color for gradient
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setGradientColors(int primaryRgb, int secondaryRgb);

    /**
     * Sets the colors of this role to {@link RoleColors#DEFAULT_HOLOGRAPHIC}.
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction useHolographicStyle();

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link lonter.jfa.api.Permission#ADMINISTRATOR admin} of the parent {@link lonter.jfa.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         The varargs {@link lonter.jfa.api.Permission Permissions} for the new role
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...) Permission.getRaw(Permission...)
     */
    @NotNull
    @CheckReturnValue
    default RoleAction setPermissions(@Nullable Permission... permissions) {
        if (permissions != null) {
            Checks.noneNull(permissions, "Permissions");
        }

        return setPermissions(permissions == null ? null : Permission.getRaw(permissions));
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link lonter.jfa.api.Permission#ADMINISTRATOR admin} of the parent {@link lonter.jfa.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         A {@link java.util.Collection Collection} of {@link lonter.jfa.api.Permission Permissions} for the new role
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     * @throws IllegalArgumentException
     *         If any of the provided permissions is {@code null}
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection) Permission.getRaw(Collection)
     * @see    java.util.EnumSet EnumSet
     */
    @NotNull
    @CheckReturnValue
    default RoleAction setPermissions(@Nullable Collection<Permission> permissions) {
        if (permissions != null) {
            Checks.noneNull(permissions, "Permissions");
        }

        return setPermissions(permissions == null ? null : Permission.getRaw(permissions));
    }

    /**
     * Sets the Permissions the new Role should have.
     * This will only allow permissions that the current account already holds unless
     * the account is owner or {@link lonter.jfa.api.Permission#ADMINISTRATOR admin} of the parent {@link lonter.jfa.api.entities.Guild Guild}.
     *
     * @param  permissions
     *         The raw {@link lonter.jfa.api.Permission Permissions} value for the new role.
     *         To retrieve this use {@link lonter.jfa.api.Permission#getRawValue()}
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not hold one of the specified permissions
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    lonter.jfa.api.Permission#getRawValue()
     * @see    lonter.jfa.api.Permission#getRaw(java.util.Collection)
     * @see    lonter.jfa.api.Permission#getRaw(lonter.jfa.api.Permission...)
     */
    @NotNull
    @CheckReturnValue
    RoleAction setPermissions(@Nullable Long permissions);

    /**
     * Sets the {@link lonter.jfa.api.entities.Icon Icon} of this {@link lonter.jfa.api.entities.Role Role}.
     * This icon will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  icon
     *         The new icon for this {@link lonter.jfa.api.entities.Role Role}
     *         or {@code null} to reset
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setIcon(@Nullable Icon icon);

    /**
     * Sets the Unicode Emoji of this {@link lonter.jfa.api.entities.Role Role} instead of a custom image.
     * This emoji will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  emoji
     *         The new Unicode emoji for this {@link lonter.jfa.api.entities.Role Role}
     *         or {@code null} to reset
     *
     * @return The current RoleAction, for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    RoleAction setIcon(@Nullable String emoji);

    /**
     * Sets the Unicode Emoji of this {@link lonter.jfa.api.entities.Role Role} instead of a custom image.
     * This emoji will be displayed next to the role's name in the members tab and in chat.
     *
     * @param  emoji
     *         The new Unicode emoji for this {@link lonter.jfa.api.entities.Role Role}
     *         or {@code null} to reset
     *
     * @return The current RoleAction, for chaining convenience
     *
     * @see    lonter.jfa.api.entities.emoji.Emoji#fromUnicode(String) Emoji.fromUnicode(String)
     * @see    UnicodeEmoji
     */
    @NotNull
    @CheckReturnValue
    default RoleAction setIcon(@Nullable UnicodeEmoji emoji) {
        return setIcon(emoji == null ? null : emoji.getFormatted());
    }
}
