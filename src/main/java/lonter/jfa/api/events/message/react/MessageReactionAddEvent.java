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
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.MessageReaction;
import lonter.jfa.api.entities.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a user added a reaction to a message
 * <br>This includes unicode and custom emoji
 *
 * <p>Can be used to track when a user adds a reaction to a message
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGE_REACTIONS GUILD_MESSAGE_REACTIONS} to work in guild text channels</li>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGE_REACTIONS DIRECT_MESSAGE_REACTIONS} to work in private channels</li>
 * </ul>
 */
public class MessageReactionAddEvent extends GenericMessageReactionEvent {
    private final long messageAuthorId;

    public MessageReactionAddEvent(
            @NotNull JFA api,
            long responseNumber,
            @Nullable User user,
            @Nullable Member member,
            @NotNull MessageReaction reaction,
            long userId,
            long messageAuthorId) {
        super(api, responseNumber, user, member, reaction, userId);
        this.messageAuthorId = messageAuthorId;
    }

    /**
     * The user id of the original message author.
     * <br>This might be 0 for webhook messages.
     *
     * @return The user id of the original message author.
     */
    @NotNull
    public String getMessageAuthorId() {
        return Long.toUnsignedString(messageAuthorId);
    }

    /**
     * The user id of the original message author.
     * <br>This might be 0 for webhook messages.
     *
     * @return The user id of the original message author.
     */
    public long getMessageAuthorIdLong() {
        return messageAuthorId;
    }
}
