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

package lonter.jfa.internal.entities.channel.mixin.middleman;

import lonter.jfa.api.Permission;
import lonter.jfa.api.components.MessageTopLevelComponent;
import lonter.jfa.api.entities.Message;
import lonter.jfa.api.entities.MessageEmbed;
import lonter.jfa.api.entities.MessageHistory;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.channel.unions.MessageChannelUnion;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.MessageCreateAction;
import lonter.jfa.api.requests.restaction.MessageEditAction;
import lonter.jfa.api.requests.restaction.pagination.MessagePaginationAction;
import lonter.jfa.api.requests.restaction.pagination.PinnedMessagePaginationAction;
import lonter.jfa.api.requests.restaction.pagination.ReactionPaginationAction;
import lonter.jfa.api.utils.AttachedFile;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.TimeUtil;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.messages.MessageCreateData;
import lonter.jfa.api.utils.messages.MessageEditData;
import lonter.jfa.api.utils.messages.MessagePollData;
import lonter.jfa.internal.entities.channel.mixin.ChannelMixin;
import lonter.jfa.internal.requests.RestActionImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface MessageChannelMixin<T extends MessageChannelMixin<T>>
        extends MessageChannel, MessageChannelUnion, ChannelMixin<T> {
    // ---- Default implementations of interface ----
    @NotNull
    default List<CompletableFuture<Void>> purgeMessages(@NotNull List<? extends Message> messages) {
        checkCanAccess();
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        if (!canDeleteOtherUsersMessages()) {
            for (Message m : messages) {
                if (m.getAuthor().equals(getJFA().getSelfUser())) {
                    continue;
                }

                if (getType() == ChannelType.PRIVATE) {
                    throw new IllegalStateException("Cannot delete messages of other users in a private channel");
                } else {
                    throw new InsufficientPermissionException(
                            (GuildChannel) this, Permission.MESSAGE_MANAGE, "Cannot delete messages of other users");
                }
            }
        }

        return MessageChannelUnion.super.purgeMessages(messages);
    }

    @NotNull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull long... messageIds) {
        checkCanAccess();
        if (messageIds == null || messageIds.length == 0) {
            return Collections.emptyList();
        }

        // If we can't use the bulk delete system, then use the standard purge defined in
        // MessageChannel
        if (!canDeleteOtherUsersMessages()) {
            return MessageChannelUnion.super.purgeMessagesById(messageIds);
        }

        // remove duplicates and sort messages
        List<CompletableFuture<Void>> list = new LinkedList<>();
        TreeSet<Long> bulk = new TreeSet<>(Comparator.reverseOrder());
        TreeSet<Long> norm = new TreeSet<>(Comparator.reverseOrder());
        long twoWeeksAgo =
                TimeUtil.getFluxerTimestamp(System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000) + 10000);
        for (long messageId : messageIds) {
            if (messageId > twoWeeksAgo) { // Bulk delete cannot delete messages older than 2 weeks.
                bulk.add(messageId);
            } else {
                norm.add(messageId);
            }
        }

        // delete chunks of 100 messages each
        if (!bulk.isEmpty()) {
            List<String> toDelete = new ArrayList<>(100);
            while (!bulk.isEmpty()) {
                toDelete.clear();
                for (int i = 0; i < 100 && !bulk.isEmpty(); i++) {
                    toDelete.add(Long.toUnsignedString(bulk.pollLast()));
                }

                // If we only had 1 in the bulk collection
                // then use the standard deleteMessageById request
                // as you cannot bulk delete a single message
                if (toDelete.size() == 1) {
                    list.add(deleteMessageById(toDelete.get(0)).submit());
                } else if (!toDelete.isEmpty()) {
                    list.add(bulkDeleteMessages(toDelete).submit());
                }
            }
        }

        // delete messages too old for bulk delete
        if (!norm.isEmpty()) {
            for (long message : norm) {
                list.add(deleteMessageById(message).submit());
            }
        }
        return list;
    }

    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendMessage(@NotNull CharSequence text) {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessage(text);
    }

    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.sendMessageEmbeds(embed, other);
    }

    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.sendMessageEmbeds(embeds);
    }

    @NotNull
    @Override
    default MessageCreateAction sendMessageComponents(
            @NotNull Collection<? extends MessageTopLevelComponent> components) {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessageComponents(components);
    }

    @NotNull
    @Override
    default MessageCreateAction sendMessagePoll(@NotNull MessagePollData poll) {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessagePoll(poll);
    }

    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendMessage(@NotNull MessageCreateData msg) {
        checkCanSendMessage();
        return MessageChannelUnion.super.sendMessage(msg);
    }

    @NotNull
    @CheckReturnValue
    default MessageCreateAction sendFiles(@NotNull Collection<? extends FileUpload> files) {
        checkCanSendMessage();
        checkCanSendFiles();
        return MessageChannelUnion.super.sendFiles(files);
    }

    @NotNull
    @CheckReturnValue
    default RestAction<Message> retrieveMessageById(@NotNull String messageId) {
        checkCanViewHistory();
        return MessageChannelUnion.super.retrieveMessageById(messageId);
    }

    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Void> deleteMessageById(@NotNull String messageId) {
        checkCanAccess();
        // We don't know if this is a Message sent by us or another user, so we can't run checks for
        // Permission.MESSAGE_MANAGE
        return MessageChannelUnion.super.deleteMessageById(messageId);
    }

    @NotNull
    @Override
    default MessageHistory getHistory() {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistory();
    }

    @NotNull
    @CheckReturnValue
    default MessagePaginationAction getIterableHistory() {
        checkCanViewHistory();
        return MessageChannelUnion.super.getIterableHistory();
    }

    @NotNull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAround(@NotNull String messageId, int limit) {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryAround(messageId, limit);
    }

    @NotNull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@NotNull String messageId, int limit) {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryAfter(messageId, limit);
    }

    @NotNull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@NotNull String messageId, int limit) {
        checkCanViewHistory();
        return MessageChannelUnion.super.getHistoryBefore(messageId, limit);
    }

    @NotNull
    @CheckReturnValue
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit) {
        checkCanViewHistory();
        return MessageHistory.getHistoryFromBeginning(this).limit(limit);
    }

    @NotNull
    @CheckReturnValue
    default RestAction<Void> sendTyping() {
        checkCanAccess();
        return MessageChannelUnion.super.sendTyping();
    }

    @NotNull
    @CheckReturnValue
    default RestAction<Void> addReactionById(@NotNull String messageId, @NotNull Emoji emoji) {
        checkCanAddReactions();
        return MessageChannelUnion.super.addReactionById(messageId, emoji);
    }

    @NotNull
    @CheckReturnValue
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emoji emoji) {
        checkCanRemoveReactions();
        return MessageChannelUnion.super.removeReactionById(messageId, emoji);
    }

    @NotNull
    @CheckReturnValue
    default ReactionPaginationAction retrieveReactionUsersById(@NotNull String messageId, @NotNull Emoji emoji) {
        checkCanRemoveReactions();
        return MessageChannelUnion.super.retrieveReactionUsersById(messageId, emoji);
    }

    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Void> pinMessageById(@NotNull String messageId) {
        checkCanControlMessagePins();
        return MessageChannelUnion.super.pinMessageById(messageId);
    }

    @NotNull
    @CheckReturnValue
    default AuditableRestAction<Void> unpinMessageById(@NotNull String messageId) {
        checkCanControlMessagePins();
        return MessageChannelUnion.super.unpinMessageById(messageId);
    }

    @NotNull
    @CheckReturnValue
    default PinnedMessagePaginationAction retrievePinnedMessages() {
        checkCanAccess();
        return MessageChannelUnion.super.retrievePinnedMessages();
    }

    @NotNull
    @CheckReturnValue
    default MessageEditAction editMessageById(@NotNull String messageId, @NotNull CharSequence newContent) {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageById(messageId, newContent);
    }

    @NotNull
    @CheckReturnValue
    default MessageEditAction editMessageById(@NotNull String messageId, @NotNull MessageEditData data) {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageById(messageId, data);
    }

    @NotNull
    @CheckReturnValue
    default MessageEditAction editMessageEmbedsById(
            @NotNull String messageId, @NotNull Collection<? extends MessageEmbed> newEmbeds) {
        checkCanSendMessage();
        checkCanSendMessageEmbeds();
        return MessageChannelUnion.super.editMessageEmbedsById(messageId, newEmbeds);
    }

    @NotNull
    @CheckReturnValue
    default MessageEditAction editMessageComponentsById(
            @NotNull String messageId, @NotNull Collection<? extends MessageTopLevelComponent> components) {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageComponentsById(messageId, components);
    }

    @NotNull
    @Override
    default MessageEditAction editMessageAttachmentsById(
            @NotNull String messageId, @NotNull Collection<? extends AttachedFile> attachments) {
        checkCanSendMessage();
        return MessageChannelUnion.super.editMessageAttachmentsById(messageId, attachments);
    }

    // ---- State Accessors ----
    T setLatestMessageIdLong(long latestMessageId);

    // ---- Mixin Hooks ----
    void checkCanSendMessage();

    void checkCanSendMessageEmbeds();

    void checkCanSendFiles();

    void checkCanViewHistory();

    void checkCanAddReactions();

    void checkCanRemoveReactions();

    void checkCanControlMessagePins();

    boolean canDeleteOtherUsersMessages();

    // ---- Helpers -----
    default RestActionImpl<Void> bulkDeleteMessages(Collection<String> messageIds) {
        DataObject body = DataObject.empty().put("messages", messageIds);
        Route.CompiledRoute route = Route.Messages.DELETE_MESSAGES.compile(getId());
        return new RestActionImpl<>(getJFA(), route, body);
    }
}
