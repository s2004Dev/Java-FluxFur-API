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

package lonter.jfa.api.managers.channel.attribute;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.IPermissionHolder;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.ICategorizableChannel;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;
import lonter.jfa.api.managers.channel.ChannelManager;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager abstraction to set the {@link Category Parent Category} of a {@link ICategorizableChannel categorizable channel}.
 *
 * @param <T> The channel type
 * @param <M> The manager type
 */
public interface ICategorizableChannelManager<
                T extends ICategorizableChannel, M extends ICategorizableChannelManager<T, M>>
        extends ChannelManager<T, M>, IPermissionContainerManager<T, M>, IPositionableChannelManager<T, M> {
    /**
     * Sets the <b><u>{@link Category Parent Category}</u></b>
     * of the selected {@link GuildChannel GuildChannel}.
     *
     * @param  category
     *         The new parent for the selected {@link GuildChannel GuildChannel}
     *
     * @throws IllegalStateException
     *         If the target is a category itself
     * @throws IllegalArgumentException
     *         If the provided category is not from the same Guild
     *
     * @return ChannelManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    M setParent(@Nullable Category category);

    /**
     * Syncs all {@link PermissionOverride PermissionOverrides} of this GuildChannel with
     * its parent ({@link Category Category}).
     *
     * <p>After this operation, all {@link PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the parent.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This behaves as if calling {@link #sync(IPermissionContainer)} with this GuildChannel's {@link ICategorizableChannel#getParentCategory()} Parent}.
     *
     * @throws  IllegalStateException
     *          If this GuildChannel has no parent
     * @throws  lonter.jfa.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://fluxer.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Fluxer Documentation - Permission Syncing</a>
     */
    @NotNull
    @CheckReturnValue
    default M sync() {
        if (!(getChannel() instanceof ICategorizableChannel)) {
            throw new IllegalStateException(
                    "sync() requires that the channel be categorizable as it syncs the channel to the parent category.");
        }

        ICategorizableChannel categorizableChannel = (ICategorizableChannel) getChannel();
        if (categorizableChannel.getParentCategory() == null) {
            throw new IllegalStateException("sync() requires a parent category");
        }
        return sync(categorizableChannel.getParentCategory());
    }

    /**
     * Syncs all {@link PermissionOverride PermissionOverrides} of this GuildChannel with
     * the given ({@link GuildChannel GuildChannel}).
     *
     * <p>After this operation, all {@link PermissionOverride PermissionOverrides}
     * will be exactly the same as the ones from the syncSource.
     * <br><b>That means that all current PermissionOverrides are lost!</b>
     *
     * <p>This will only work for Channels of the same {@link Guild Guild}!.
     *
     * @param   syncSource
     *          The GuildChannel from where all PermissionOverrides should be copied from
     *
     * @throws  IllegalArgumentException
     *          If the given snySource is {@code null}, this GuildChannel or from a different Guild.
     * @throws  lonter.jfa.api.exceptions.InsufficientPermissionException
     *          If the currently logged in account does not have {@link Permission#MANAGE_PERMISSIONS Permission.MANAGE_PERMISSIONS}
     *          in this channel or {@link IPermissionHolder#canSync(IPermissionContainer, IPermissionContainer)} is false for the self member.
     *
     * @return  ChannelManager for chaining convenience
     *
     * @see     <a href="https://fluxer.com/developers/docs/topics/permissions#permission-syncing" target="_blank">Fluxer Documentation - Permission Syncing</a>
     */
    @NotNull
    @CheckReturnValue
    M sync(@NotNull IPermissionContainer syncSource);
}
