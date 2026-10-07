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
import lonter.jfa.api.entities.channel.middleman.GuildMessageChannel;
import lonter.jfa.api.events.message.MessageBulkDeleteEvent;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.WebSocketClient;

import java.util.List;
import java.util.stream.Collectors;

public class MessageBulkDeleteHandler extends SocketHandler {
    public MessageBulkDeleteHandler(JFAImpl api) {
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

            guild = api.getGuildById(guildId);
            if (guild == null) {
                EventCache.LOG.debug(
                        "Caching MESSAGE_DELETE event for guild that is not currently cached. GuildID: {}", guildId);
                api.getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
                return null;
            }
        }
        long channelId = content.getLong("channel_id");

        if (getJFA().isBulkDeleteSplittingEnabled()) {
            SocketHandler handler = getJFA().getClient().getHandlers().get("MESSAGE_DELETE");
            content.getArray("ids").forEach(id -> {
                handler.handle(
                        responseNumber,
                        DataObject.empty()
                                .put("t", "MESSAGE_DELETE")
                                .put(
                                        "d",
                                        DataObject.empty()
                                                .put("channel_id", Long.toUnsignedString(channelId))
                                                .put("id", id)));
            });
        } else {
            GuildMessageChannel channel = getJFA().getChannelById(GuildMessageChannel.class, channelId);
            if (channel == null) {
                if (guild != null) {
                    GuildChannel guildChannel = guild.getGuildChannelById(channelId);
                    if (guildChannel != null) {
                        WebSocketClient.LOG.debug(
                                "Discarding MESSAGE_DELETE event for unexpected channel type. Channel: {}",
                                guildChannel);
                        return null;
                    }
                }

                getJFA().getEventCache()
                        .cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug(
                        "Received a Bulk Message Delete for a GuildMessageChannel that is not yet cached.");
                return null;
            }

            if (getJFA().getGuildSetupController().isLocked(channel.getGuild().getIdLong())) {
                return channel.getGuild().getIdLong();
            }

            DataArray array = content.getArray("ids");
            List<String> messages = array.stream(DataArray::getString).collect(Collectors.toList());
            getJFA().handleEvent(new MessageBulkDeleteEvent(getJFA(), responseNumber, channel, messages));
        }
        return null;
    }
}
