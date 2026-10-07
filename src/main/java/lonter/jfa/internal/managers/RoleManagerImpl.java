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

package lonter.jfa.internal.managers;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.RoleColors;
import lonter.jfa.api.exceptions.HierarchyException;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.RoleManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.PermissionUtil;
import okhttp3.RequestBody;

import java.util.Collection;
import java.util.EnumSet;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class RoleManagerImpl extends ManagerBase<RoleManager> implements RoleManager {
    protected Role role;

    protected String name;
    protected RoleColors colors;
    protected long permissions;
    protected boolean hoist;
    protected boolean mentionable;
    protected Icon icon;
    protected String emoji;

    /**
     * Creates a new RoleManager instance
     *
     * @param role
     *        {@link lonter.jfa.api.entities.Role Role} that should be modified
     */
    public RoleManagerImpl(Role role) {
        super(role.getJFA(), Route.Roles.MODIFY_ROLE.compile(role.getGuild().getId(), role.getId()));
        this.role = role;
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    @NotNull
    @Override
    public Role getRole() {
        Role realRole = role.getGuild().getRoleById(role.getIdLong());
        if (realRole != null) {
            role = realRole;
        }
        return role;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        if ((fields & COLOR) == COLOR) {
            this.colors = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl reset() {
        super.reset();
        this.name = null;
        this.colors = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setName(@NotNull String name) {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setPermissions(long perms) {
        long selfPermissions = PermissionUtil.getEffectivePermission(getGuild().getSelfMember());
        setupPermissions();
        long missingPerms = perms; // include permissions we want to set to
        missingPerms &= ~selfPermissions; // exclude permissions we have
        missingPerms &= ~this.permissions; // exclude permissions the role has
        // if any permissions remain, we have an issue
        if (missingPerms != 0 && isPermissionChecksEnabled()) {
            EnumSet<Permission> permissionList = Permission.getPermissions(missingPerms);
            if (!permissionList.isEmpty()) {
                throw new InsufficientPermissionException(
                        getGuild(), permissionList.iterator().next());
            }
        }
        this.permissions = perms;
        set |= PERMISSION;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setColor(int rgb) {
        this.colors = new RoleColors(rgb, Role.DEFAULT_COLOR_RAW, Role.DEFAULT_COLOR_RAW);
        set |= COLOR;
        return this;
    }

    @NotNull
    @Override
    public RoleManager setColors(RoleColors colors) {
        this.colors = colors;
        set |= COLOR;
        return this;
    }

    @NotNull
    @Override
    public RoleManager setGradientColors(int primaryRgb, int secondaryRgb) {
        this.colors = new RoleColors(primaryRgb, secondaryRgb, Role.DEFAULT_COLOR_RAW);
        set |= COLOR;
        return this;
    }

    @NotNull
    @Override
    public RoleManager useHolographicStyle() {
        this.colors = RoleColors.DEFAULT_HOLOGRAPHIC;
        set |= COLOR;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setHoisted(boolean hoisted) {
        this.hoist = hoisted;
        set |= HOIST;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setMentionable(boolean mentionable) {
        this.mentionable = mentionable;
        set |= MENTIONABLE;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setIcon(Icon icon) {
        this.icon = icon;
        this.emoji = null;
        set |= ICON;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl setIcon(String emoji) {
        this.emoji = emoji;
        this.icon = null;
        set |= ICON;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl givePermissions(@NotNull Collection<Permission> perms) {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions | Permission.getRaw(perms));
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleManagerImpl revokePermissions(@NotNull Collection<Permission> perms) {
        Checks.noneNull(perms, "Permissions");
        setupPermissions();
        return setPermissions(this.permissions & ~Permission.getRaw(perms));
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty().put("name", getRole().getName());
        if (shouldUpdate(NAME)) {
            object.put("name", name);
        }
        if (shouldUpdate(PERMISSION)) {
            object.put("permissions", permissions);
        }
        if (shouldUpdate(HOIST)) {
            object.put("hoist", hoist);
        }
        if (shouldUpdate(MENTIONABLE)) {
            object.put("mentionable", mentionable);
        }
        if (shouldUpdate(COLOR)) {
            object.put("colors", colors);
        }
        if (shouldUpdate(ICON)) {
            object.put("icon", icon == null ? null : icon.getEncoding());
            object.put("unicode_emoji", emoji);
        }
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions() {
        Member selfMember = getGuild().getSelfMember();
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_ROLES);
        }
        if (!selfMember.canInteract(getRole())) {
            throw new HierarchyException("Cannot modify a role that is higher or equal in hierarchy");
        }
        return super.checkPermissions();
    }

    private void setupPermissions() {
        if (!shouldUpdate(PERMISSION)) {
            this.permissions = getRole().getPermissionsRaw();
        }
    }
}
