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

package lonter.jfa.api.events.message.react;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.events.message.GenericMessageEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the reactions of a message have been cleared by a moderator
 *
 * <p>Can be used to detect when the reactions of a message are removed by a moderator
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} to work in guild text channels</li>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGE_REACTIONS DIRECT_MESSAGE_REACTIONS} to work in private channels</li>
 * </ul>
 */
public class MessageReactionRemoveAllEvent extends GenericMessageEvent {
    public MessageReactionRemoveAllEvent(
            @NotNull JFA api, long responseNumber, long messageId, @NotNull MessageChannel channel) {
        super(api, responseNumber, messageId, channel);
    }
}
