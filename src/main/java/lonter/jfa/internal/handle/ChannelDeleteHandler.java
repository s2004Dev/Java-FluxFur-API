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

import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.events.channel.ChannelDeleteEvent;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.requests.WebSocketClient;

public class ChannelDeleteHandler extends SocketHandler {
    public ChannelDeleteHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        ChannelType type = ChannelType.fromId(content.getInt("type"));

        long guildId = 0;
        if (type.isGuild()) {
            guildId = content.getLong("guild_id");
            if (getJFA().getGuildSetupController().isLocked(guildId)) {
                return guildId;
            }
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        long channelId = content.getLong("id");

        if (guild == null) {
            PrivateChannel channel = getJFA().getChannelsView().remove(ChannelType.PRIVATE, channelId);
            if (channel == null) {
                WebSocketClient.LOG.debug(
                        "CHANNEL_DELETE attempted to delete a private channel that is not yet cached. JSON: {}",
                        content);
            }
            return null;
        }

        GuildChannel channel = guild.getChannelById(GuildChannel.class, channelId);
        if (channel == null) {
            WebSocketClient.LOG.debug(
                    "CHANNEL_DELETE attempted to delete a guild channel that is not yet cached. JSON: {}", content);
            return null;
        }

        guild.uncacheChannel(channel, false);

        getJFA().handleEvent(new ChannelDeleteEvent(getJFA(), responseNumber, channel));

        // Deleting any scheduled events associated to the deleted channel as they are deleted when
        // the channel gets deleted.
        // There is no delete event for the deletion of scheduled events in this case, so we do this
        // to keep the cache in sync.
        String location = Long.toUnsignedString(channelId);
        guild.getScheduledEventsView().stream()
                .filter(scheduledEvent -> scheduledEvent.getType().isChannel()
                        && scheduledEvent.getLocation().equals(location))
                .forEach(scheduledEvent -> guild.getScheduledEventsView().remove(scheduledEvent.getIdLong()));

        getJFA().getEventCache().clear(EventCache.Type.CHANNEL, channelId);
        return null;
    }
}
