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

import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.events.role.RoleDeleteEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.entities.RoleImpl;
import lonter.jfa.internal.entities.emoji.RichCustomEmojiImpl;
import lonter.jfa.internal.requests.WebSocketClient;

public class GuildRoleDeleteHandler extends SocketHandler {
    public GuildRoleDeleteHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("GUILD_ROLE_DELETE was received for a Guild that is not yet cached: {}", content);
            return null;
        }

        long roleId = content.getLong("role_id");
        RoleImpl removedRole = (RoleImpl) guild.getRolesView().get(roleId);
        if (removedRole == null) {
            // getJFA().getEventCache().cache(EventCache.Type.ROLE, roleId, () ->
            // handle(responseNumber, allContent));
            WebSocketClient.LOG.debug("GUILD_ROLE_DELETE was received for a Role that is not yet cached: {}", content);
            return null;
        }

        // Allow for position to still be retrievable in event handling
        removedRole.freezePosition();
        guild.getRolesView().remove(roleId);

        // Now that the role is removed from the Guild, remove it from all users and emojis.
        guild.getMembersView().forEach(m -> {
            MemberImpl member = (MemberImpl) m;
            member.getRoleSet().remove(removedRole);
        });

        for (RichCustomEmoji emoji : guild.getEmojiCache()) {
            RichCustomEmojiImpl impl = (RichCustomEmojiImpl) emoji;
            impl.getRoleSet().remove(removedRole);
        }

        getJFA().handleEvent(new RoleDeleteEvent(getJFA(), responseNumber, removedRole));
        getJFA().getEventCache().clear(EventCache.Type.ROLE, roleId);
        return null;
    }
}
