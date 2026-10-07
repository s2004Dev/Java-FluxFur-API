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

package lonter.jfa.api.requests.restaction.order;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.channel.ChannelType;
import lonter.jfa.api.entities.channel.concrete.Category;
import lonter.jfa.api.entities.channel.middleman.GuildChannel;

import java.util.EnumSet;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link OrderAction OrderAction}
 * to modify the order of {@link GuildChannel Channels} for a {@link lonter.jfa.api.entities.Guild Guild}.
 * <br>To apply the changes you must finish the {@link lonter.jfa.api.requests.RestAction RestAction}.
 *
 * <p>Before you can use any of the {@code move} methods
 * you must use either {@link #selectPosition(Object) selectPosition(GuildChannel)} or {@link #selectPosition(int)}!
 *
 * @see   lonter.jfa.api.entities.Guild
 * @see   lonter.jfa.api.entities.Guild#modifyTextChannelPositions()
 * @see   lonter.jfa.api.entities.Guild#modifyVoiceChannelPositions()
 * @see   lonter.jfa.api.entities.Guild#modifyCategoryPositions()
 * @see   CategoryOrderAction
 */
public interface ChannelOrderAction extends OrderAction<GuildChannel, ChannelOrderAction> {
    /**
     * The {@link lonter.jfa.api.entities.Guild Guild} which holds
     * the channels from {@link #getCurrentOrder()}
     *
     * @return The corresponding {@link lonter.jfa.api.entities.Guild Guild}
     */
    @NotNull
    Guild getGuild();

    /**
     * The sorting bucket for this order action.
     * <br>Multiple different {@link ChannelType ChannelTypes} can
     * share a common sorting bucket.
     *
     * @return The sorting bucket
     */
    int getSortBucket();

    /**
     * The {@link ChannelType ChannelTypes} for the {@link #getSortBucket() sorting bucket}.
     *
     * @return The channel types
     *
     * @see    ChannelType#fromSortBucket(int)
     */
    @NotNull
    default EnumSet<ChannelType> getChannelTypes() {
        return ChannelType.fromSortBucket(getSortBucket());
    }

    /**
     * Set the parent category for the currently selected channel.
     *
     * @param  category
     *         The new parent category, or null to not have any category
     * @param  syncPermissions
     *         Whether to sync the permissions of the channel to the new category
     *
     * @throws IllegalStateException
     *         If no entity has been selected yet, use {@link #selectPosition(Object)}
     * @throws IllegalArgumentException
     *         If the provided category is not in the same guild as the channel
     *
     * @return The current ChannelOrderAction
     */
    @NotNull
    @CheckReturnValue
    ChannelOrderAction setCategory(@Nullable Category category, boolean syncPermissions);

    /**
     * Set the parent category for the currently selected channel.
     *
     * <p>By default, this will not sync the permissions with the new category.
     * You can use {@link #setCategory(Category, boolean)} to sync permissions.
     *
     * @param  category
     *         The new parent category, or null to not have any category
     *
     * @throws IllegalStateException
     *         If no entity has been selected yet, use {@link #selectPosition(Object)}
     * @throws IllegalArgumentException
     *         If the provided category is not in the same guild as the channel
     *
     * @return The current ChannelOrderAction
     */
    @NotNull
    @CheckReturnValue
    default ChannelOrderAction setCategory(@Nullable Category category) {
        return setCategory(category, false);
    }
}
