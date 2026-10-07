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
import lonter.jfa.api.events.message.poll.MessagePollVoteAddEvent;
import lonter.jfa.api.events.message.poll.MessagePollVoteRemoveEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.WebSocketClient;

public class MessagePollVoteHandler extends SocketHandler {
    private final boolean add;

    public MessagePollVoteHandler(JFAImpl api, boolean add) {
        super(api);
        this.add = add;
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long answerId = content.getLong("answer_id");
        long userId = content.getUnsignedLong("user_id");
        long messageId = content.getUnsignedLong("message_id");
        long channelId = content.getUnsignedLong("channel_id");
        long guildId = content.getUnsignedLong("guild_id", 0);

        if (api.getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        Guild guild = api.getGuildById(guildId);
        MessageChannel channel = api.getChannelById(MessageChannel.class, channelId);
        if (channel == null) {
            if (guild != null) {
                GuildChannel actual = guild.getGuildChannelById(channelId);
                if (actual != null) {
                    WebSocketClient.LOG.debug(
                            "Dropping message poll vote event for unexpected channel of type {}", actual.getType());
                    return null;
                }
            }

            if (guildId != 0) {
                api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, responseNumber, allContent, this::handle);
                EventCache.LOG.debug("Received a vote for a channel that JFA does not currently have cached");
                return null;
            }

            channel = getJFA().getEntityBuilder()
                    .createPrivateChannel(DataObject.empty().put("id", channelId));
        }

        if (add) {
            api.handleEvent(new MessagePollVoteAddEvent(channel, responseNumber, messageId, userId, answerId));
        } else {
            api.handleEvent(new MessagePollVoteRemoveEvent(channel, responseNumber, messageId, userId, answerId));
        }

        return null;
    }
}
