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

package it.lonter.jfa.internal.handle;

import it.lonter.jfa.api.entities.channel.Channel;
import it.lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import it.lonter.jfa.api.events.channel.ChannelDeleteEvent;
import it.lonter.jfa.api.utils.data.DataObject;
import it.lonter.jfa.internal.JFAImpl;
import it.lonter.jfa.internal.entities.GuildImpl;
import it.lonter.jfa.internal.requests.WebSocketClient;
import it.lonter.jfa.internal.utils.cache.ChannelCacheViewImpl;

public class ThreadDeleteHandler extends SocketHandler {
    public ThreadDeleteHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        long guildId = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        long threadId = content.getLong("id");

        ChannelCacheViewImpl<Channel> channelsView = getJFA().getChannelsView();
        ThreadChannel thread = channelsView.ofType(ThreadChannel.class).getElementById(threadId);
        if (thread == null || guild == null) {
            WebSocketClient.LOG.debug(
                    "THREAD_DELETE attempted to delete a thread that is not yet cached. JSON: {}", content);
            return null;
        }

        channelsView.remove(thread.getType(), threadId);
        guild.getChannelView().remove(thread);

        getJFA().handleEvent(new ChannelDeleteEvent(getJFA(), responseNumber, thread));

        getJFA().getEventCache().clear(EventCache.Type.CHANNEL, threadId);
        return null;
    }
}
