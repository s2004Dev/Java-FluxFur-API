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

import lonter.jfa.api.entities.User;
import lonter.jfa.api.events.guild.GuildBanEvent;
import lonter.jfa.api.events.guild.GuildUnbanEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;

public class GuildBanHandler extends SocketHandler {
    private final boolean banned;

    public GuildBanHandler(JFAImpl api, boolean banned) {
        super(api);
        this.banned = banned;
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(id)) {
            return id;
        }

        DataObject userJson = content.getObject("user");
        GuildImpl guild = (GuildImpl) getJFA().getGuildById(id);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "Received Guild Member {} event for a Guild not yet cached.", banned ? "Ban" : "Unban");
            return null;
        }

        User user = getJFA().getEntityBuilder().createUser(userJson);

        if (banned) {
            getJFA().handleEvent(new GuildBanEvent(getJFA(), responseNumber, guild, user));
        } else {
            getJFA().handleEvent(new GuildUnbanEvent(getJFA(), responseNumber, guild, user));
        }
        return null;
    }
}
