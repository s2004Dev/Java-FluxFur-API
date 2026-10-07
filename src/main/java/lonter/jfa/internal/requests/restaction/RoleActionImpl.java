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

package lonter.jfa.internal.requests.restaction;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.RoleColors;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.Request;
import lonter.jfa.api.requests.Response;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.RoleAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoleActionImpl extends AuditableRestActionImpl<Role> implements RoleAction {
    protected final Guild guild;
    protected Long permissions;
    protected String name = null;
    protected RoleColors colors = null;
    protected Boolean hoisted = null;
    protected Boolean mentionable = null;
    protected Icon icon = null;
    protected String emoji = null;

    /**
     * Creates a new RoleAction instance
     *
     * @param  guild
     *         The {@link lonter.jfa.api.entities.Guild Guild} for which the Role should be created.
     */
    public RoleActionImpl(Guild guild) {
        super(guild.getJFA(), Route.Roles.CREATE_ROLE.compile(guild.getId()));
        this.guild = guild;
    }

    @NotNull
    @Override
    public RoleActionImpl setCheck(BooleanSupplier checks) {
        return (RoleActionImpl) super.setCheck(checks);
    }

    @NotNull
    @Override
    public RoleActionImpl timeout(long timeout, @NotNull TimeUnit unit) {
        return (RoleActionImpl) super.timeout(timeout, unit);
    }

    @NotNull
    @Override
    public RoleActionImpl deadline(long timestamp) {
        return (RoleActionImpl) super.deadline(timestamp);
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setName(String name) {
        if (name != null) {
            Checks.notEmpty(name, "Name");
            Checks.notLonger(name, 100, "Name");
        }
        this.name = name;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setHoisted(Boolean hoisted) {
        this.hoisted = hoisted;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setMentionable(Boolean mentionable) {
        this.mentionable = mentionable;
        return this;
    }

    @NotNull
    @Override
    public RoleAction setGradientColors(int primaryRgb, int secondaryRgb) {
        this.colors = new RoleColors(primaryRgb, secondaryRgb, Role.DEFAULT_COLOR_RAW);
        return this;
    }

    @NotNull
    @Override
    public RoleAction useHolographicStyle() {
        this.colors = RoleColors.DEFAULT_HOLOGRAPHIC;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setColor(Integer rgb) {
        this.colors = new RoleColors(
                rgb == null ? Role.DEFAULT_COLOR_RAW : rgb, Role.DEFAULT_COLOR_RAW, Role.DEFAULT_COLOR_RAW);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleAction setColors(@Nullable RoleColors colors) {
        this.colors = colors;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setPermissions(Long permissions) {
        if (permissions != null) {
            for (Permission p : Permission.getPermissions(permissions)) {
                checkPermission(p);
            }
        }
        this.permissions = permissions;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setIcon(Icon icon) {
        this.icon = icon;
        this.emoji = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public RoleActionImpl setIcon(String emoji) {
        this.emoji = emoji;
        this.icon = null;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        if (name != null) {
            object.put("name", name);
        }
        if (colors != null && !colors.isDefault()) {
            object.put("colors", colors);
        }
        if (permissions != null) {
            object.put("permissions", permissions);
        }
        if (hoisted != null) {
            object.put("hoist", hoisted);
        }
        if (mentionable != null) {
            object.put("mentionable", mentionable);
        }
        if (icon != null) {
            object.put("icon", icon.getEncoding());
        }
        if (emoji != null) {
            object.put("unicode_emoji", emoji);
        }

        return getRequestBody(object);
    }

    @Override
    protected void handleSuccess(Response response, Request<Role> request) {
        request.onSuccess(
                api.getEntityBuilder().createRole((GuildImpl) guild, response.getObject(), guild.getIdLong()));
    }

    private void checkPermission(Permission permission) {
        if (!guild.getSelfMember().hasPermission(permission)) {
            throw new InsufficientPermissionException(guild, permission);
        }
    }
}
