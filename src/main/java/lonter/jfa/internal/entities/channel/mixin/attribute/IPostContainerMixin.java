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

package lonter.jfa.internal.entities.channel.mixin.attribute;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.channel.attribute.IPostContainer;
import lonter.jfa.api.entities.channel.forums.ForumTag;
import lonter.jfa.api.requests.restaction.ForumPostAction;
import lonter.jfa.api.requests.restaction.ThreadChannelAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.messages.MessageCreateBuilder;
import lonter.jfa.api.utils.messages.MessageCreateData;
import lonter.jfa.internal.requests.restaction.ForumPostActionImpl;
import lonter.jfa.internal.utils.cache.SortedSnowflakeCacheViewImpl;

import org.jetbrains.annotations.NotNull;

public interface IPostContainerMixin<T extends IPostContainerMixin<T>>
        extends IPostContainer, IThreadContainerMixin<T> {
    @NotNull
    @Override
    SortedSnowflakeCacheViewImpl<ForumTag> getAvailableTagCache();

    @NotNull
    @Override
    default ForumPostAction createForumPost(@NotNull String name, @NotNull MessageCreateData message) {
        checkAttached();
        checkPermission(Permission.MESSAGE_SEND);
        return new ForumPostActionImpl(this, name, new MessageCreateBuilder().applyData(message));
    }

    @NotNull
    @Override
    default ThreadChannelAction createThreadChannel(@NotNull String name) {
        throw new UnsupportedOperationException(
                "You cannot create threads without a message payload in forum/media channels! Use createForumPost(...) instead.");
    }

    @NotNull
    @Override
    default ThreadChannelAction createThreadChannel(@NotNull String name, @NotNull String messageId) {
        throw new UnsupportedOperationException(
                "You cannot create threads without a message payload in forum/media channels! Use createForumPost(...) instead.");
    }

    T setDefaultReaction(DataObject emoji);

    T setDefaultSortOrder(int defaultSortOrder);

    T setFlags(int flags);

    int getRawSortOrder();

    int getRawFlags();
}
