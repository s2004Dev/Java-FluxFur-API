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

import lonter.jfa.api.audit.AuditLogEntry;
import lonter.jfa.api.events.guild.GuildAuditLogEntryCreateEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;

public class GuildAuditLogEntryCreateHandler extends SocketHandler {
    public GuildAuditLogEntryCreateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long id = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(id)) {
            return id;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(id);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, id, responseNumber, allContent, this::handle);
            EventCache.LOG.debug("Received Guild Audit Log Create event for a Guild not yet cached. GuildId: {}", id);
            return null;
        }

        AuditLogEntry entry = api.getEntityBuilder().createAuditLogEntry(guild, content, null, null);

        api.handleEvent(new GuildAuditLogEntryCreateEvent(api, responseNumber, entry));

        return null;
    }
}
