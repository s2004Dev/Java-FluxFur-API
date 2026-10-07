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

import lonter.jfa.api.entities.Role;
import lonter.jfa.api.events.guild.member.GuildMemberUpdateEvent;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.MemberImpl;

import java.util.LinkedList;
import java.util.List;

public class GuildMemberUpdateHandler extends SocketHandler {

    public GuildMemberUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(id)) {
            return id;
        }

        DataObject userJson = content.getObject("user");
        long userId = userJson.getLong("id");
        GuildImpl guild = (GuildImpl) getJFA().getGuildById(id);
        if (guild == null) {
            // Do not cache this here, it will be outdated once we receive the GUILD_CREATE and
            // could cause invalid cache
            // getJFA().getEventCache().cache(EventCache.Type.GUILD, userId, responseNumber,
            // allContent, this::handle);
            EventCache.LOG.debug(
                    "Got GuildMember update but JFA currently does not have the Guild cached. Ignoring. {}", content);
            return null;
        }

        MemberImpl member = (MemberImpl) guild.getMembersView().get(userId);
        if (member == null) {
            member = getJFA().getEntityBuilder().createMember(guild, content);
        } else {
            List<Role> newRoles = toRolesList(guild, content.getArray("roles"));
            getJFA().getEntityBuilder().updateMember(guild, member, content, newRoles);
        }

        getJFA().getEntityBuilder().updateMemberCache(member);
        getJFA().handleEvent(new GuildMemberUpdateEvent(getJFA(), responseNumber, member));
        return null;
    }

    private List<Role> toRolesList(GuildImpl guild, DataArray array) {
        LinkedList<Role> roles = new LinkedList<>();
        for (int i = 0; i < array.length(); i++) {
            long id = array.getLong(i);
            Role r = guild.getRolesView().get(id);
            if (r != null) {
                roles.add(r);
            } else {
                getJFA().getEventCache().cache(EventCache.Type.ROLE, id, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Got GuildMember update but one of the Roles for the Member is not yet cached.");
                return null;
            }
        }
        return roles;
    }
}
