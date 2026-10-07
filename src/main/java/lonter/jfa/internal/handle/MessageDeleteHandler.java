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
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.events.message.MessageDeleteEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.channel.concrete.ThreadChannelImpl;
import lonter.jfa.internal.requests.WebSocketClient;

public class MessageDeleteHandler extends SocketHandler {

    public MessageDeleteHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        Guild guild = null;
        if (!content.isNull("guild_id")) {
            long guildId = content.getLong("guild_id");
            if (getJFA().getGuildSetupController().isLocked(guildId)) {
                return guildId;
            }

            guild = getJFA().getGuildById(guildId);
            if (guild == null) {
                getJFA().getEventCache()
                        .cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Got message delete for a guild that is not yet cached. GuildId: {}", guildId);
                return null;
            }
        }

        long messageId = content.getLong("id");
        long channelId = content.getLong("channel_id");

        MessageChannel channel = getJFA().getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            // If fluxer adds message support for unexpected types in the future,
            // drop the event instead of caching it
            if (guild != null) {
                GuildChannel actual = guild.getGuildChannelById(channelId);
                if (actual != null) {
                    WebSocketClient.LOG.debug(
                            "Dropping MESSAGE_DELETE for unexpected channel of type {}", actual.getType());
                    return null;
                }
            }

            getJFA().getEventCache()
                    .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
            EventCache.LOG.debug(
                    "Got message delete for a channel/group that is not yet cached. ChannelId: {}", channelId);
            return null;
        }

        if (channel.getType().isThread()) {
            ThreadChannelImpl gThread = (ThreadChannelImpl) channel;

            gThread.setMessageCount(Math.max(0, gThread.getMessageCount() - 1));
            // Not decrementing totalMessageCount since that should include deleted as well
        }

        getJFA().handleEvent(new MessageDeleteEvent(getJFA(), responseNumber, messageId, channel));
        return null;
    }
}
