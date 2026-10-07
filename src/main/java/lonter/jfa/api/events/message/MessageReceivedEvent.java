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
import lonter.jfa.api.entities.Webhook;
import lonter.jfa.api.entities.channel.concrete.PrivateChannel;
import lonter.jfa.api.entities.channel.concrete.TextChannel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a Message was received in a {@link lonter.jfa.api.entities.channel.middleman.MessageChannel MessageChannel}.
 * <br>This includes {@link TextChannel TextChannel} and {@link PrivateChannel PrivateChannel}!
 *
 * <p>Can be used to detect that a Message is received in either a guild- or private channel. Providing a MessageChannel and Message.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires at least one of the following intents (Will not fire at all if neither is enabled):
 * <ul>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#GUILD_MESSAGES GUILD_MESSAGES} to work in guild text channels</li>
 *     <li>{@link lonter.jfa.api.requests.GatewayIntent#DIRECT_MESSAGES DIRECT_MESSAGES} to work in private channels</li>
 * </ul>
 */
public class MessageReceivedEvent extends GenericMessageEvent {
    private final Message message;

    public MessageReceivedEvent(@NotNull JFA api, long responseNumber, @NotNull Message message) {
        super(api, responseNumber, message.getIdLong(), message.getChannel());
        this.message = message;
    }

    /**
     * The received {@link lonter.jfa.api.entities.Message Message} object.
     *
     * @return The received {@link lonter.jfa.api.entities.Message Message} object.
     */
    @NotNull
    public Message getMessage() {
        return message;
    }

    /**
     * The Author of the Message received as {@link lonter.jfa.api.entities.User User} object.
     * <br>This will be never-null but might be a fake user if Message was sent via Webhook (Guild only).
     * See {@link Webhook#getDefaultUser()}.
     *
     * @return The Author of the Message.
     *
     * @see #isWebhookMessage()
     */
    @NotNull
    public User getAuthor() {
        return message.getAuthor();
    }

    /**
     * The Author of the Message received as {@link lonter.jfa.api.entities.Member Member} object.
     * <br>This will be {@code null} in case of Message being received in
     * a {@link PrivateChannel PrivateChannel}
     * or {@link #isWebhookMessage() isWebhookMessage()} returning {@code true}.
     *
     * @return The Author of the Message as null-able Member object.
     *
     * @see    #isWebhookMessage()
     */
    @Nullable
    public Member getMember() {
        return message.getMember();
    }

    /**
     * Whether or not the Message received was sent via a Webhook.
     * <br>This is a shortcut for {@code getMessage().isWebhookMessage()}.
     *
     * @return True, if the Message was sent via Webhook
     */
    public boolean isWebhookMessage() {
        return getMessage().isWebhookMessage();
    }
}
