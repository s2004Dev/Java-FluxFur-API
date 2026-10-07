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
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.attribute.IThreadContainer;
import lonter.jfa.api.entities.channel.unions.IThreadContainerUnion;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.ThreadChannelAction;
import lonter.jfa.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import lonter.jfa.internal.entities.channel.mixin.middleman.GuildChannelMixin;
import lonter.jfa.internal.requests.restaction.ThreadChannelActionImpl;
import lonter.jfa.internal.requests.restaction.pagination.ThreadChannelPaginationActionImpl;
import lonter.jfa.internal.utils.Checks;

import org.jetbrains.annotations.NotNull;

public interface IThreadContainerMixin<T extends IThreadContainerMixin<T>>
        extends IThreadContainer, IThreadContainerUnion, GuildChannelMixin<T> {
    // ---- Default implementations of interface ----
    @NotNull
    @Override
    default ThreadChannelAction createThreadChannel(@NotNull String name, boolean isPrivate) {
        Checks.notNull(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        if (isPrivate) {
            checkPermission(Permission.CREATE_PRIVATE_THREADS);
        } else {
            checkPermission(Permission.CREATE_PUBLIC_THREADS);
        }

        ChannelType threadType = isPrivate
                ? ChannelType.GUILD_PRIVATE_THREAD
                : getType() == ChannelType.TEXT ? ChannelType.GUILD_PUBLIC_THREAD : ChannelType.GUILD_NEWS_THREAD;

        return new ThreadChannelActionImpl(this, name, threadType);
    }

    @NotNull
    @Override
    default ThreadChannelAction createThreadChannel(@NotNull String name, long messageId) {
        Checks.notNull(name, "Name");
        name = name.trim();
        Checks.notEmpty(name, "Name");
        Checks.notLonger(name, 100, "Name");

        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.CREATE_PUBLIC_THREADS);

        return new ThreadChannelActionImpl(this, name, Long.toUnsignedString(messageId));
    }

    @NotNull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels() {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_PUBLIC_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJFA(), route, this, false);
    }

    @NotNull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels() {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);
        checkPermission(Permission.MANAGE_THREADS);

        Route.CompiledRoute route = Route.Channels.LIST_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJFA(), route, this, false);
    }

    @NotNull
    @Override
    default ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels() {
        checkAttached();
        Checks.checkAccess(getGuild().getSelfMember(), this);
        checkPermission(Permission.MESSAGE_HISTORY);

        Route.CompiledRoute route = Route.Channels.LIST_JOINED_PRIVATE_ARCHIVED_THREADS.compile(getId());
        return new ThreadChannelPaginationActionImpl(getJFA(), route, this, true);
    }

    T setDefaultThreadSlowmode(int slowmode);
}
