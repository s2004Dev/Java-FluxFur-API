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

package lonter.jfa.internal.handle;

import lonter.jfa.api.entities.RoleColors;
import lonter.jfa.api.entities.RoleIcon;
import lonter.jfa.api.events.role.update.*;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.AbstractEntityBuilder;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.RoleImpl;

import java.util.Objects;

public class GuildRoleUpdateHandler extends SocketHandler {
    public GuildRoleUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        DataObject rolejson = content.getObject("role");
        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a Role Update for a Guild that is not yet cached: {}", content);
            return null;
        }

        long roleId = rolejson.getLong("id");
        RoleImpl role = (RoleImpl) guild.getRolesView().get(roleId);
        if (role == null) {
            getJFA().getEventCache().cache(EventCache.Type.ROLE, roleId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received a Role Update for Role that is not yet cached: {}", content);
            return null;
        }

        String name = rolejson.getString("name");
        RoleColors colors = AbstractEntityBuilder.createRoleColors(rolejson.getObject("colors"));

        int position = rolejson.getInt("position");
        long permissions = rolejson.getLong("permissions");
        boolean hoisted = rolejson.getBoolean("hoist");
        boolean mentionable = rolejson.getBoolean("mentionable");
        String iconId = rolejson.getString("icon", null);
        String emoji = rolejson.getString("unicode_emoji", null);

        rolejson.optObject("tags").ifPresent(role::setTags);

        if (!Objects.equals(name, role.getName())) {
            String oldName = role.getName();
            role.setName(name);
            getJFA().handleEvent(new RoleUpdateNameEvent(getJFA(), responseNumber, role, oldName));
        }
        if (!colors.equals(role.getColors())) {
            RoleColors oldColors = role.getColors();
            role.setPrimaryColor(colors.getPrimaryRaw());
            role.setSecondaryColor(colors.getSecondaryRaw());
            role.setTertiaryColor(colors.getTertiaryRaw());
            getJFA().handleEvent(new RoleUpdateColorsEvent(getJFA(), responseNumber, role, oldColors));

            if (oldColors.getPrimaryRaw() != colors.getPrimaryRaw()) {
                @SuppressWarnings("deprecation")
                RoleUpdateColorEvent event =
                        new RoleUpdateColorEvent(getJFA(), responseNumber, role, oldColors.getPrimaryRaw());
                getJFA().handleEvent(event);
            }
        }
        if (!Objects.equals(position, role.getPositionRaw())) {
            int oldPosition = role.getPosition();
            int oldPositionRaw = role.getPositionRaw();
            role.setRawPosition(position);
            getJFA().handleEvent(
                            new RoleUpdatePositionEvent(getJFA(), responseNumber, role, oldPosition, oldPositionRaw));
        }
        if (!Objects.equals(permissions, role.getPermissionsRaw())) {
            long oldPermissionsRaw = role.getPermissionsRaw();
            role.setRawPermissions(permissions);
            getJFA().handleEvent(new RoleUpdatePermissionsEvent(getJFA(), responseNumber, role, oldPermissionsRaw));
        }

        if (hoisted != role.isHoisted()) {
            boolean wasHoisted = role.isHoisted();
            role.setHoisted(hoisted);
            getJFA().handleEvent(new RoleUpdateHoistedEvent(getJFA(), responseNumber, role, wasHoisted));
        }
        if (mentionable != role.isMentionable()) {
            boolean wasMentionable = role.isMentionable();
            role.setMentionable(mentionable);
            getJFA().handleEvent(new RoleUpdateMentionableEvent(getJFA(), responseNumber, role, wasMentionable));
        }

        RoleIcon oldIcon = role.getIcon();
        RoleIcon newIcon = iconId == null && emoji == null ? null : new RoleIcon(iconId, emoji, roleId);
        if (!Objects.equals(oldIcon, newIcon)) {
            role.setIcon(newIcon);
            getJFA().handleEvent(new RoleUpdateIconEvent(getJFA(), responseNumber, role, oldIcon));
        }
        return null;
    }
}
