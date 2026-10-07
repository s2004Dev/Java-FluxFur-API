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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.MemberImpl;
import lonter.jfa.internal.requests.WebSocketClient;
import lonter.jfa.internal.utils.Helpers;

public class GuildMembersChunkHandler extends SocketHandler {
    public GuildMembersChunkHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        DataArray members = content.getArray("members");
        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        if (guild != null) {
            if (api.getClient().getChunkManager().handleChunk(guildId, content)) {
                return null;
            }
            WebSocketClient.LOG.debug(
                    "Received member chunk for guild that is already in cache. GuildId: {} Count: {} Index: {}/{}",
                    guildId,
                    members.length(),
                    content.getInt("chunk_index"),
                    content.getInt("chunk_count"));
            // Chunk handling
            EntityBuilder builder = getJFA().getEntityBuilder();
            TLongObjectMap<DataObject> presences = content.optArray("presences")
                    .map(it -> Helpers.convertToMap(o -> o.getObject("user").getUnsignedLong("id"), it))
                    .orElseGet(TLongObjectHashMap::new);
            for (int i = 0; i < members.length(); i++) {
                DataObject object = members.getObject(i);
                long userId = object.getObject("user").getUnsignedLong("id");
                DataObject presence = presences.get(userId);
                MemberImpl member = builder.createMember(guild, object, null, presence);
                builder.updateMemberCache(member);
            }
            return null;
        }
        getJFA().getGuildSetupController().onMemberChunk(guildId, content);
        return null;
    }
}
