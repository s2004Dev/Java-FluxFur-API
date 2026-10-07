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

package lonter.jfa.api.entities.sticker;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.managers.GuildStickerManager;
import lonter.jfa.api.requests.ErrorResponse;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import org.jetbrains.annotations.Contract;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom guild sticker created by a user.
 */
public interface GuildSticker extends RichSticker {
    @NotNull
    @Override
    default Type getType() {
        return Type.GUILD;
    }

    /**
     * Whether this sticker is currently available.
     * <br>A sticker becomes unavailable when the boost level of a guild drops and the slot becomes unusable.
     *
     * @return True, if this sticker is available
     */
    boolean isAvailable();

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    long getGuildIdLong();

    /**
     * The ID of the guild this sticker belongs to.
     *
     * @return The guild id
     */
    @NotNull
    default String getGuildId() {
        return Long.toUnsignedString(getGuildIdLong());
    }

    /**
     * The {@link Guild} this sticker belongs to.
     * <br>This is null if the guild is not cached on creation,
     * Which is often the case for {@link lonter.jfa.api.JFA#retrieveSticker(StickerSnowflake) JFA.retrieveSticker(...)}.
     *
     * @return Possibly-null guild of the sticker
     */
    @Nullable
    Guild getGuild();

    /**
     * The user who created this sticker.
     * <br>This is null if the sticker is retrieved from cache,
     * since the owner is only provided for explicitly requested stickers.
     *
     * @return Possibly-null sticker owner
     *
     * @see    #retrieveOwner()
     */
    @Nullable
    User getOwner();

    /**
     * Retrieves the sticker owner.
     * <br>If {@link #getOwner()} is present, this will directly return the owner in a completed {@link RestAction} without making a request.
     * The user information might be outdated, you can use {@link CacheRestAction#useCache(boolean) action.useCache(false)} to force an update.
     *
     * <p>Possible {@link ErrorResponse ErrorResponses} include:
     * <ul>
     *     <li>{@link ErrorResponse#MISSING_PERMISSIONS MISSING_PERMISSIONS}
     *     <br>The request was attempted after the account lost {@link Permission#MANAGE_GUILD_EXPRESSIONS Permission.MANAGE_GUILD_EXPRESSIONS} in the guild</li>
     * </ul>
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS Permission.MANAGE_GUILD_EXPRESSIONS} in the guild.
     *
     * @return {@link CacheRestAction} - Type: {@link User}
     */
    @NotNull
    @CheckReturnValue
    CacheRestAction<User> retrieveOwner();

    /**
     * Deletes this sticker from the guild.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS MANAGE_GUILD_EXPRESSIONS} in the guild.
     *
     * @return {@link AuditableRestAction}
     */
    @NotNull
    @CheckReturnValue
    AuditableRestAction<Void> delete();

    /**
     * Modify this sticker using {@link GuildStickerManager}.
     * <br>You can update multiple fields at once, by calling the respective setters before executing the request.
     *
     * @throws lonter.jfa.api.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have {@link Permission#MANAGE_GUILD_EXPRESSIONS MANAGE_GUILD_EXPRESSIONS} in the guild.
     *
     * @return {@link GuildStickerManager}
     */
    @NotNull
    @CheckReturnValue
    @Contract("->new")
    GuildStickerManager getManager();
}
