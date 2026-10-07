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
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.middleman.GuildMessageChannel;
import lonter.jfa.api.entities.channel.unions.GuildMessageChannelUnion;
import lonter.jfa.api.entities.emoji.Emoji;
import lonter.jfa.api.entities.sticker.StickerSnowflake;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.MessageCreateAction;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.api.utils.TimeUtil;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.MessageCreateActionImpl;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.PermissionUtil;

import java.util.Collection;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface GuildMessageChannelMixin<T extends GuildMessageChannelMixin<T>>
        extends GuildMessageChannel, GuildMessageChannelUnion, GuildChannelMixin<T>, MessageChannelMixin<T> {

    // ---- Default implementations of interface ----
    @NotNull
    @CheckReturnValue
    default RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds) {
        checkCanAccess();
        checkPermission(
                Permission.MESSAGE_MANAGE,
                "Must have MESSAGE_MANAGE in order to bulk delete messages in this channel regardless of author.");

        if (messageIds.size() < 2 || messageIds.size() > 100) {
            throw new IllegalArgumentException("Must provide at least 2 or at most 100 messages to be deleted.");
        }

        long twoWeeksAgo = TimeUtil.getFluxerTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)));
        for (String id : messageIds) {
            Checks.check(
                    MiscUtil.parseSnowflake(id) > twoWeeksAgo, "Message Id provided was older than 2 weeks. Id: " + id);
        }

        return bulkDeleteMessages(messageIds);
    }

    @NotNull
    @Override
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emoji emoji, @NotNull User user) {
        Checks.isSnowflake(messageId, "Message ID");
        Checks.notNull(emoji, "Emoji");
        Checks.notNull(user, "User");

        checkCanAccess();
        if (!getJFA().getSelfUser().equals(user)) {
            checkPermission(Permission.MESSAGE_MANAGE);
        }

        String targetUser;
        if (user.equals(getJFA().getSelfUser())) {
            targetUser = "@me";
        } else {
            targetUser = user.getId();
        }

        Route.CompiledRoute route =
                Route.Messages.REMOVE_REACTION.compile(getId(), messageId, emoji.getAsReactionCode(), targetUser);
        return new RestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(@NotNull String messageId) {
        Checks.isSnowflake(messageId, "Message ID");

        checkCanAccess();
        checkPermission(Permission.MESSAGE_MANAGE);

        Route.CompiledRoute route = Route.Messages.REMOVE_ALL_REACTIONS.compile(getId(), messageId);
        return new RestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull Emoji emoji) {
        Checks.notNull(messageId, "Message ID");
        Checks.notNull(emoji, "Emoji");

        checkCanAccess();
        checkPermission(Permission.MESSAGE_MANAGE);

        Route.CompiledRoute route =
                Route.Messages.CLEAR_EMOJI_REACTIONS.compile(getId(), messageId, emoji.getAsReactionCode());
        return new RestActionImpl<>(getJFA(), route);
    }

    @NotNull
    @Override
    default MessageCreateAction sendStickers(@NotNull Collection<? extends StickerSnowflake> stickers) {
        checkCanSendMessage();
        Checks.notEmpty(stickers, "Stickers");
        Checks.noneNull(stickers, "Stickers");
        return new MessageCreateActionImpl(this).setStickers(stickers);
    }

    // ---- Default implementation of parent mixins hooks ----

    default void checkCanSendMessage() {
        checkCanAccess();
        if (getType().isThread()) {
            checkPermission(Permission.MESSAGE_SEND_IN_THREADS);
        } else {
            checkPermission(Permission.MESSAGE_SEND);
        }
    }

    default void checkCanSendMessageEmbeds() {
        checkCanAccess();
        checkPermission(Permission.MESSAGE_EMBED_LINKS);
    }

    default void checkCanSendFiles() {
        checkCanAccess();
        checkPermission(Permission.MESSAGE_ATTACH_FILES);
    }

    default void checkCanViewHistory() {
        checkCanAccess();
        checkPermission(Permission.MESSAGE_HISTORY);
    }

    default void checkCanAddReactions() {
        checkCanAccess();
        checkPermission(Permission.MESSAGE_ADD_REACTION);
        checkPermission(Permission.MESSAGE_HISTORY, "You need MESSAGE_HISTORY to add reactions to a message");
    }

    default void checkCanRemoveReactions() {
        checkCanAccess();
        checkPermission(Permission.MESSAGE_HISTORY, "You need MESSAGE_HISTORY to remove reactions from a message");
    }

    default void checkCanControlMessagePins() {
        checkCanAccess();
        PermissionUtil.checkWithDeadline(
                this,
                PermissionUtil.FEB_23_2026_DEADLINE,
                /* old */ Permission.MESSAGE_MANAGE,
                /* new */ Permission.PIN_MESSAGES);
    }

    default boolean canDeleteOtherUsersMessages() {
        return hasPermission(Permission.MESSAGE_MANAGE);
    }
}
