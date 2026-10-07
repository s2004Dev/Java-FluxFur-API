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

import lonter.jfa.api.events.guild.member.GuildMemberJoinEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.MemberImpl;

public class GuildMemberAddHandler extends SocketHandler {

    public GuildMemberAddHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("guild_id");
        boolean setup = getJFA().getGuildSetupController().onAddMember(id, content);
        if (setup) {
            return null;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(id);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Caching member for guild that is not yet cached. Guild ID: {} JSON: {}", id, content);
            return null;
        }

        // Update memberCount
        guild.onMemberAdd();
        MemberImpl member = getJFA().getEntityBuilder().createMember(guild, content);
        getJFA().getEntityBuilder().updateMemberCache(member);
        getJFA().handleEvent(new GuildMemberJoinEvent(getJFA(), responseNumber, member));
        return null;
    }
}
