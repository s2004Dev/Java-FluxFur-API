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

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.events.guild.invite.GuildInviteDeleteEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;

public class InviteDeleteHandler extends SocketHandler {
    public InviteDeleteHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getUnsignedLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }
        Guild guild = getJFA().getGuildById(guildId);
        if (guild == null) {
            EventCache.LOG.debug("Caching INVITE_DELETE for unknown guild {}", guildId);
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }
        long channelId = content.getUnsignedLong("channel_id");
        GuildChannel channel = guild.getGuildChannelById(channelId);
        if (channel == null) {
            EventCache.LOG.debug("Caching INVITE_DELETE for unknown channel {} in guild {}", channelId, guildId);
            getJFA().getEventCache()
                    .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            return null;
        }

        String code = content.getString("code");
        getJFA().handleEvent(new GuildInviteDeleteEvent(getJFA(), responseNumber, code, channel));
        return null;
    }
}
