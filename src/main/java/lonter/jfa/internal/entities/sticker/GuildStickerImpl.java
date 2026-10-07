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

package lonter.jfa.internal.entities.sticker;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.exceptions.ErrorResponseException;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.GuildStickerManager;
import lonter.jfa.api.requests.ErrorResponse;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.managers.GuildStickerManagerImpl;
import lonter.jfa.internal.requests.DeferredRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildStickerImpl extends RichStickerImpl implements GuildSticker {
    private final long guildId;
    private final JFA jfa;
    private Guild guild;
    private User owner;

    private boolean available;

    public GuildStickerImpl(
            long id,
            StickerFormat format,
            String name,
            Set<String> tags,
            String description,
            boolean available,
            long guildId,
            JFA jfa,
            User owner) {
        super(id, format, name, tags, description);
        this.available = available;
        this.guildId = guildId;
        this.jfa = jfa;
        this.guild = jfa.getGuildById(guildId);
        this.owner = owner;
    }

    @NotNull
    @Override
    public GuildSticker asGuildSticker() {
        return this;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public long getGuildIdLong() {
        return guildId;
    }

    @Nullable
    @Override
    public Guild getGuild() {
        Guild realGuild = jfa.getGuildById(guildId);
        if (realGuild != null) {
            guild = realGuild;
        }
        return guild;
    }

    @Nullable
    @Override
    public User getOwner() {
        if (owner != null) {
            User realOwner = jfa.getUserById(owner.getIdLong());
            if (realOwner != null) {
                owner = realOwner;
            }
        }
        return owner;
    }

    @NotNull
    @Override
    public CacheRestAction<User> retrieveOwner() {
        Guild g = getGuild();
        if (g != null && !g.getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(g, Permission.MANAGE_GUILD_EXPRESSIONS);
        }
        return new DeferredRestAction<>(jfa, User.class, this::getOwner, () -> {
            Route.CompiledRoute route = Route.Stickers.GET_GUILD_STICKER.compile(getGuildId(), getId());
            return new RestActionImpl<>(jfa, route, (response, request) -> {
                DataObject json = response.getObject();
                return this.owner = json.optObject("user")
                        .map(user -> ((JFAImpl) jfa).getEntityBuilder().createUser(json.getObject("user")))
                        .orElseThrow(() -> ErrorResponseException.create(ErrorResponse.MISSING_PERMISSIONS, response));
            });
        });
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        if (guild != null) {
            return guild.deleteSticker(this);
        }
        Route.CompiledRoute route = Route.Stickers.DELETE_GUILD_STICKER.compile(getGuildId(), getId());
        return new AuditableRestActionImpl<>(jfa, route);
    }

    @NotNull
    @Override
    public GuildStickerManager getManager() {
        return new GuildStickerManagerImpl(getGuild(), getGuildIdLong(), this);
    }

    public GuildStickerImpl setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public GuildStickerImpl copy() {
        return new GuildStickerImpl(id, format, name, tags, description, available, guildId, jfa, owner);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .setName(name)
                .addMetadata("guild", getGuildId())
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, format, name, getType(), tags, description, available, guildId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GuildStickerImpl)) {
            return false;
        }
        GuildStickerImpl other = (GuildStickerImpl) obj;
        return id == other.id
                && format == other.format
                && getType() == other.getType()
                && available == other.available
                && guildId == other.guildId
                && Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Helpers.deepEqualsUnordered(tags, other.tags);
    }
}
