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

package lonter.jfa.api.entities.channel.middleman;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.concrete.TextChannel;
import lonter.jfa.api.entities.channel.concrete.ThreadChannel;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.entities.sticker.Sticker;
import lonter.jfa.api.entities.sticker.StickerSnowflake;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.exceptions.MissingAccessException;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.MessageCreateAction;
import lonter.jfa.internal.utils.Checks;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents all message channels present in guilds.
 *
 * <p>This includes channels that are not included in {@link StandardGuildMessageChannel}, such as {@link ThreadChannel}.
 *
 * @see StandardGuildMessageChannel
 */
public interface GuildMessageChannel extends GuildChannel, MessageChannel {
    @Override
    default boolean canTalk() {
        return canTalk(getGuild().getSelfMember());
    }

    /**
     * Whether the specified {@link lonter.jfa.api.entities.Member}
     * can send messages in this channel.
     * <br>Checks for both {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} and
     * {@link lonter.jfa.api.Permission#MESSAGE_SEND Permission.MESSAGE_SEND}.
     *
     * @param  member
     *         The Member to check
     *
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this channel is a thread,
     *         and the bot {@link #isDetached() isn't in the guild}.
     * @throws lonter.jfa.api.exceptions.MissingEntityInteractionPermissionsException
     *         if the bot {@link #isDetached() isn't in the guild},
     *         and the combination of the provided Member and this GuildChannel doesn't have permission data,
     *         see {@link lonter.jfa.api.exceptions.MissingEntityInteractionPermissionsException MissingEntityInteractionPermissionsException}.
     *
     * @return True, if the specified member is able to read and send messages in this channel
     */
    boolean canTalk(@NotNull Member member);

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link TextChannel TextChannel}.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to remove the reaction from
     * @param  emoji
     *         The emoji to remove
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.RestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emoji emoji, @NotNull User user);

    /**
     * Attempts to remove the reaction from a message represented by the specified {@code messageId}
     * in this MessageChannel.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The request was attempted after the account lost access to the
     *         {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed, or after {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL}
     *         was revoked in the {@link TextChannel TextChannel}
     *     <br>Also can happen if the account lost the {@link lonter.jfa.api.Permission#MESSAGE_HISTORY Permission.MESSAGE_HISTORY}</li>
     *
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MESSAGE_ADD_REACTION Permission.MESSAGE_ADD_REACTION} in the
     *         {@link TextChannel TextChannel}.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_EMOJI}
     *     <br>The provided unicode character does not refer to a known emoji unicode character.
     *     <br>Proper unicode characters for emojis can be found here:
     *         <a href="https://unicode.org/emoji/charts/full-emoji-list.html" target="_blank">Emoji Table</a></li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The provided {@code messageId} is unknown in this MessageChannel, either due to the id being invalid, or
     *         the message it referred to has already been deleted.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>The request was attempted after the channel was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The messageId to remove the reaction from
     * @param  emoji
     *         The emoji to remove
     * @param  user
     *         The target user of which to remove from
     *
     * @throws java.lang.IllegalArgumentException
     *         <ul>
     *             <li>If provided {@code messageId} is {@code null} or empty.</li>
     *             <li>If provided {@code emoji} is {@code null}.</li>
     *         </ul>
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.RestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(long messageId, @NotNull Emoji emoji, @NotNull User user) {
        return removeReactionById(Long.toUnsignedString(messageId), emoji, user);
    }

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link lonter.jfa.api.entities.Message#delete()} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link lonter.jfa.api.entities.Message#delete()} method instead.
     *
     * <br><p>You must have the Permission {@link lonter.jfa.api.Permission#MESSAGE_MANAGE MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when using {@link lonter.jfa.api.entities.MessageHistory MessageHistory} to delete a large amount
     * of messages. If you have a large amount of messages but only their message Ids, please use {@link #deleteMessagesByIds(Collection)}
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>if any of the provided messages does not exist</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messages
     *         The collection of messages to delete.
     *
     * @throws IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If this account does not have {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #deleteMessagesByIds(Collection)
     * @see    #purgeMessages(List)
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> deleteMessages(@NotNull Collection<Message> messages) {
        Checks.notEmpty(messages, "Messages collection");

        return deleteMessagesByIds(messages.stream().map(ISnowflake::getId).collect(Collectors.toList()));
    }

    /**
     * Bulk deletes a list of messages.
     * <b>This is not the same as calling {@link MessageChannel#deleteMessageById(String)} in a loop.</b>
     * <br>This is much more efficient, but it has a different ratelimit. You may call this once per second per Guild.
     *
     * <p>Must be at least 2 messages and not be more than 100 messages at a time.
     * <br>If you only have 1 message, use the {@link lonter.jfa.api.entities.Message#delete()} method instead.
     *
     * <br><p>You must have {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel to use
     * this function.
     *
     * <p>This method is best used when you have a large amount of messages but only their message Ids. If you are using
     * {@link lonter.jfa.api.entities.MessageHistory MessageHistory} or have {@link lonter.jfa.api.entities.Message Message}
     * objects, it would be easier to use {@link #deleteMessages(java.util.Collection)}.
     *
     * <p>Possible ErrorResponses include:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_CHANNEL UNKNOWN_CHANNEL}
     *     <br>if this channel was deleted</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>if any of the provided messages does not exist</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>if we were removed from the guild</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The send request was attempted after the account lost
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in the channel.</li>
     * </ul>
     *
     * @param  messageIds
     *         The message ids for the messages to delete.
     *
     * @throws java.lang.IllegalArgumentException
     *         If the size of the list less than 2 or more than 100 messages.
     * @throws java.lang.NumberFormatException
     *         If any of the provided ids cannot be parsed by {@link Long#parseLong(String)}
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If this account does not have {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     *
     * @see    #deleteMessages(Collection)
     * @see    #purgeMessagesById(List)
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds);

    /**
     * Attempts to remove all reactions from a message with the specified {@code messageId} in this TextChannel
     * <br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The clear-reactions request was attempted after the account lost access to the {@link TextChannel TextChannel}
     *         due to {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked, or the
     *         account lost access to the {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The clear-reactions request was attempted after the account lost {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *         in the {@link TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The clear-reactions request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The not-empty valid message id
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     * @throws java.lang.IllegalArgumentException
     *         If the provided {@code id} is {@code null} or empty.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> clearReactionsById(@NotNull String messageId);

    /**
     * Attempts to remove all reactions from a message with the specified {@code messageId} in this TextChannel
     * <br>This is useful for moderator commands that wish to remove all reactions at once from a specific message.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The clear-reactions request was attempted after the account lost access to the {@link TextChannel TextChannel}
     *         due to {@link lonter.jfa.api.Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} being revoked, or the
     *         account lost access to the {@link lonter.jfa.api.entities.Guild Guild}
     *         typically due to being kicked or removed.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The clear-reactions request was attempted after the account lost {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE}
     *         in the {@link TextChannel TextChannel} when adding the reaction.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *         The clear-reactions request was attempted after the Message had been deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The message id
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have
     *         {@link lonter.jfa.api.Permission#MESSAGE_MANAGE Permission.MESSAGE_MANAGE} in this channel.
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link lonter.jfa.api.requests.restaction.AuditableRestAction AuditableRestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> clearReactionsById(long messageId) {
        return clearReactionsById(Long.toUnsignedString(messageId));
    }

    /**
     * Removes all reactions for the specified emoji.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link lonter.jfa.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided {@link Emoji} was deleted or doesn't exist.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  emoji
     *         The {@link Emoji} to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull Emoji emoji);

    /**
     * Removes all reactions for the specified emoji.
     *
     * <p>The following {@link lonter.jfa.api.requests.ErrorResponse ErrorResponses} are possible:
     * <ul>
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#MISSING_ACCESS MISSING_ACCESS}
     *     <br>The currently logged in account lost access to the channel by either being removed from the guild
     *         or losing the {@link lonter.jfa.api.Permission#VIEW_CHANNEL VIEW_CHANNEL} permission</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_EMOJI UNKNOWN_EMOJI}
     *     <br>The provided {@link Emoji} was deleted or doesn't exist.</li>
     *
     *     <li>{@link lonter.jfa.api.requests.ErrorResponse#UNKNOWN_MESSAGE UNKNOWN_MESSAGE}
     *     <br>The message was deleted.</li>
     * </ul>
     *
     * @param  messageId
     *         The id for the target message
     * @param  emoji
     *         The {@link Emoji} to remove reactions for
     *
     * @throws InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MESSAGE_MANAGE} in the channel
     * @throws IllegalArgumentException
     *         If provided with null
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link RestAction}
     */
    @NotNull
    @CheckReturnValue
    default RestAction<Void> clearReactionsById(long messageId, @NotNull Emoji emoji) {
        return clearReactionsById(Long.toUnsignedString(messageId), emoji);
    }

    /**
     * Send up to 3 stickers in this channel.
     * <br>Bots can only send {@link GuildSticker GuildStickers} from the same {@link lonter.jfa.api.entities.Guild}.
     * Bots cannot use {@link lonter.jfa.api.entities.sticker.StandardSticker StandardStickers}.
     *
     * @param  stickers
     *         Collection of 1-3 stickers to send
     *
     * @throws MissingAccessException
     *         If the currently logged in account does not have {@link Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in this channel
     * @throws InsufficientPermissionException
     *         <ul>
     *           <li>If this is a {@link ThreadChannel} and the bot does not have {@link Permission#MESSAGE_SEND_IN_THREADS Permission.MESSAGE_SEND_IN_THREADS}</li>
     *           <li>If this is not a {@link ThreadChannel} and the bot does not have {@link Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *         </ul>
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the list is empty or has more than 3 stickers</li>
     *           <li>If null is provided</li>
     *         </ul>
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link MessageCreateAction}
     *
     * @see    Sticker#fromId(long)
     */
    @NotNull
    @CheckReturnValue
    MessageCreateAction sendStickers(@NotNull Collection<? extends StickerSnowflake> stickers);

    /**
     * Send up to 3 stickers in this channel.
     * <br>Bots can only send {@link GuildSticker GuildStickers} from the same {@link lonter.jfa.api.entities.Guild}.
     * Bots cannot use {@link lonter.jfa.api.entities.sticker.StandardSticker StandardStickers}.
     *
     * @param  stickers
     *         The 1-3 stickers to send
     *
     * @throws MissingAccessException
     *         If the currently logged in account does not have {@link Permission#VIEW_CHANNEL Permission.VIEW_CHANNEL} in this channel
     * @throws InsufficientPermissionException
     *         <ul>
     *           <li>If this is a {@link ThreadChannel} and the bot does not have {@link Permission#MESSAGE_SEND_IN_THREADS Permission.MESSAGE_SEND_IN_THREADS}</li>
     *           <li>If this is not a {@link ThreadChannel} and the bot does not have {@link Permission#MESSAGE_SEND Permission.MESSAGE_SEND}</li>
     *         </ul>
     * @throws IllegalArgumentException
     *         <ul>
     *           <li>If any of the provided stickers is a {@link GuildSticker},
     *               which is either {@link GuildSticker#isAvailable() unavailable} or from a different guild.</li>
     *           <li>If the list is empty or has more than 3 stickers</li>
     *           <li>If null is provided</li>
     *         </ul>
     * @throws lonter.jfa.api.exceptions.DetachedEntityException
     *         If this entity is {@link #isDetached() detached}
     *
     * @return {@link MessageCreateAction}
     *
     * @see    Sticker#fromId(long)
     */
    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendStickers(@NotNull StickerSnowflake... stickers) {
        Checks.notEmpty(stickers, "Stickers");
        return sendStickers(Arrays.asList(stickers));
    }
}
