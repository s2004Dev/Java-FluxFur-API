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
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a Message was edited in a {@link lonter.jfa.api.entities.channel.middleman.MessageChannel MessageChannel}.
 *
 * <p>Can be used to detect a Message is edited in either a private or guild channel. Providing a MessageChannel and Message.
 * <br>This also includes whether a message is being pinned.
 *
 * <p><b>JFA does not have a cache for messages and is not able to provide previous information due to limitations by the
 * Fluxer API!</b>
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} to work in guild text channels</li>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} to work in private channels</li>
 * </ul>
 */
public class MessageUpdateEvent extends GenericMessageEvent {
    private final Message message;

    public MessageUpdateEvent(@NotNull JFA api, long responseNumber, @NotNull Message message) {
        super(api, responseNumber, message.getIdLong(), message.getChannel());
        this.message = message;
    }

    /**
     * The {@link lonter.jfa.api.entities.Message Message} that was updated
     * <br>Note: Messages in JFA are not updated, they are immutable and will not change their state.
     *
     * @return The updated Message
     */
    @NotNull
    public Message getMessage() {
        return message;
    }

    /**
     * The author of the Message.
     *
     * @return The message author
     *
     * @see    lonter.jfa.api.entities.User User
     */
    @NotNull
    public User getAuthor() {
        return message.getAuthor();
    }

    /**
     * Member instance for the author of this message or {@code null} if this
     * was not in a Guild.
     *
     * @return The Member instance for the author or null
     */
    @Nullable
    public Member getMember() {
        return message.getMember();
    }
}
