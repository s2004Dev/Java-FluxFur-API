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

package lonter.jfa.api.events.message;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.middleman.GuildMessageChannel;
import lonter.jfa.api.entities.channel.unions.GuildMessageChannelUnion;
import lonter.jfa.api.events.Event;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a bulk deletion is executed in a {@link lonter.jfa.api.entities.channel.middleman.GuildMessageChannel GuildMessageChannel}.
 * <br>Set {@link lonter.jfa.api.JFABuilder#setBulkDeleteSplittingEnabled(boolean)} to false in order to enable this event.
 *
 * <p>Can be used to detect that a large chunk of Messages is deleted in a GuildMessageChannel. Providing a list of Message IDs and the specific GuildMessageChannel.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} to work in guild message channels</li>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} to work in private channels</li>
 * </ul>
 */
public class MessageBulkDeleteEvent extends Event {
    protected final GuildMessageChannel channel;
    protected final List<String> messageIds;

    public MessageBulkDeleteEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull GuildMessageChannel channel,
            @NotNull List<String> messageIds) {
        super(api, responseNumber);
        this.channel = channel;
        this.messageIds = Collections.unmodifiableList(messageIds);
    }

    /**
     * The {@link lonter.jfa.api.entities.channel.middleman.GuildMessageChannel GuildMessageChannel} where the messages have been deleted
     *
     * @return The TextChannel
     */
    @NotNull
    public GuildMessageChannelUnion getChannel() {
        return (GuildMessageChannelUnion) channel;
    }

    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} where the messages were deleted.
     *
     * @return The Guild
     */
    @NotNull
    public Guild getGuild() {
        return channel.getGuild();
    }

    /**
     * List of messages that have been deleted.
     *
     * @return The list of message ids
     */
    @NotNull
    public List<String> getMessageIds() {
        return messageIds;
    }
}
