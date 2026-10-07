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

package lonter.jfa.api.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.requests.CompletedRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object representing a reference in a Fluxer message.
 *
 * @see Message#getMessageReference()
 */
public class MessageReference {
    private final int type;
    private final long messageId;
    private final long channelId;
    private final long guildId;

    private final JFA api;
    private final MessageChannel channel;
    private final Guild guild;
    private Message referencedMessage;

    public MessageReference(
            int type, long messageId, long channelId, long guildId, @Nullable Message referencedMessage, JFA api) {
        this.type = type;
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.referencedMessage = referencedMessage;

        if (guildId == 0L) {
            this.channel = api.getPrivateChannelById(channelId);
        } else {
            this.channel = api.getChannelById(MessageChannel.class, channelId);
        }

        this.guild = api.getGuildById(guildId); // is null if guildId = 0 anyway

        this.api = api;
    }

    /**
     * Retrieves the referenced message for this message.
     * <br>If the message already exists, it will be returned immediately.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link TextChannel TextChannel}</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link TextChannel TextChannel}.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If this reference refers to a {@link lonter.jfa.api.entities.channel.middleman.GuildChannel GuildChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link lonter.jfa.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT} (applicable if {@code getChannel().getType().isAudio()})</li>
     *             <li>{@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws java.lang.IllegalStateException
     *         If this message reference does not have a channel
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link lonter.jfa.api.entities.Message}
     */
    @NotNull
    @CheckReturnValue
    public RestAction<Message> resolve() {
        return resolve(true);
    }

    /**
     * Retrieves the referenced message for this message.
     * <br>If the message already exists, it will be returned immediately.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link TextChannel TextChannel}</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}
     *         in the {@link TextChannel TextChannel}.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message has already been deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  update
     *         Whether to update the already stored message
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If this reference refers to a {@link lonter.jfa.api.entities.channel.middleman.GuildChannel GuildChannel} and the logged in account does not have
     *         <ul>
     *             <li>{@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}</li>
     *             <li>{@link lonter.jfa.api.Permission#VOICE_CONNECT Permission.VOICE_CONNECT} (applicable if {@code getChannel().getType().isAudio()})</li>
     *             <li>{@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *         </ul>
     * @throws java.lang.IllegalStateException
     *         If this message reference does not have a channel
     *
     * @return {@link lonter.jfa.api.requests.RestAction RestAction} - Type: {@link lonter.jfa.api.entities.Message}
     */
    @NotNull
    @CheckReturnValue
    public RestAction<Message> resolve(boolean update) {
        checkPermission(Permission.VIEW_CHANNEL);
        checkPermission(Permission.MESSAGE_HISTORY);

        if (channel == null) {
            throw new IllegalStateException("Cannot resolve a message without a channel present.");
        }

        JFAImpl jfa = (JFAImpl) getJFA();
        Message referenced = getMessage();

        if (referenced != null && !update) {
            return new CompletedRestAction<>(jfa, referenced);
        }

        Route.CompiledRoute route = Route.Messages.GET_MESSAGE.compile(getChannelId(), getMessageId());
        return new RestActionImpl<>(jfa, route, (response, request) -> {
            // channel can be null for MessageReferences,
            // but we've already checked for that above,
            // so it is nonnull here
            Message created = jfa.getEntityBuilder().createMessageWithChannel(response.getObject(), channel, false);
            this.referencedMessage = created;
            return created;
        });
    }

    /**
     * The resolved message, if available.
     *
     * <p>This will have different meaning depending on the {@link Message#getType() type} of message.
     * Usually, this is a {@link MessageType#INLINE_REPLY INLINE_REPLY} reference.
     * This can be null even if the type is {@link MessageType#INLINE_REPLY INLINE_REPLY}, when the message it references doesn't exist or fluxer wasn't able to resolve it in time.
     *
     * @return The referenced message, or null if this is not available
     *
     * @see    #resolve()
     */
    @Nullable
    public Message getMessage() {
        return referencedMessage;
    }

    /**
     * The channel from which this message originates.
     * <br>Messages from other guilds can be referenced, in which case JFA may not have the channel cached.
     *
     * @return The origin channel for this message reference, or null if this is not available
     *
     * @see    #getChannelId()
     */
    @Nullable
    public MessageChannelUnion getChannel() {
        return (MessageChannelUnion) channel;
    }

    /**
     * The guild for this reference.
     * <br>This will be null if the message did not come from a guild, the guild was not provided, or JFA did not have the guild cached
     *
     * @return The guild, or null if this is not available
     *
     * @see    #getGuildId()
     */
    @Nullable
    public Guild getGuild() {
        return guild;
    }

    /**
     * The message reference type id
     *
     * @return The raw type id
     */
    public int getTypeRaw() {
        return type;
    }

    /**
     * The type of this message reference
     *
     * @return The {@link MessageReferenceType} or {@link MessageReferenceType#UNKNOWN}
     */
    @NotNull
    public MessageReferenceType getType() {
        return MessageReferenceType.fromId(type);
    }

    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    public long getMessageIdLong() {
        return messageId;
    }

    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    public long getChannelIdLong() {
        return channelId;
    }

    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    public long getGuildIdLong() {
        return guildId;
    }

    /**
     * Returns the message id for this reference, or 0 if no message id was provided.
     *
     * @return The message id, or 0.
     */
    @NotNull
    public String getMessageId() {
        return Long.toUnsignedString(getMessageIdLong());
    }

    /**
     * Returns the channel id for this reference, or 0 if no channel id was provided.
     *
     * @return The channel id, or 0.
     */
    @NotNull
    public String getChannelId() {
        return Long.toUnsignedString(getChannelIdLong());
    }

    /**
     * Returns the guild id for this reference, or 0 if no guild id was provided.
     *
     * @return The guild id, or 0.
     */
    @NotNull
    public String getGuildId() {
        return Long.toUnsignedString(getGuildIdLong());
    }

    /**
     * Returns the JFA instance related to this message reference.
     *
     * @return The corresponding JFA instance
     */
    @NotNull
    public JFA getJFA() {
        return api;
    }

    private void checkPermission(Permission permission) {
        if (guild == null || !(channel instanceof GuildChannel)) {
            return;
        }

        Member selfMember = guild.getSelfMember();
        GuildChannel guildChannel = (GuildChannel) channel;

        Checks.checkAccess(selfMember, guildChannel);
        if (!selfMember.hasPermission(guildChannel, permission)) {
            throw new InsufficientPermissionException(guildChannel, permission);
        }
    }

    /**
     * The type of message reference
     */
    public enum MessageReferenceType {
        /** This message reference indicates a replied to message */
        DEFAULT(0),
        /** This message reference indicates a forwarded message */
        FORWARD(1),

        UNKNOWN(-1);

        private final int id;

        MessageReferenceType(int id) {
            this.id = id;
        }

        /**
         * Convert the raw type id to the message reference type enum
         *
         * @param  id
         *         Raw type id
         *
         * @return Enum constant of the reference type or {@link #UNKNOWN}
         */
        @NotNull
        public static MessageReferenceType fromId(int id) {
            for (MessageReferenceType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        /**
         * The raw type id used in the API.
         *
         * @return The raw type id
         */
        public int getId() {
            return id;
        }
    }
}
