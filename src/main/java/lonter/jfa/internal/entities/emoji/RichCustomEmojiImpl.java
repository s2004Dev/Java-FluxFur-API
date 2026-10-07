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

package lonter.jfa.internal.entities.emoji;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.emoji.*;
import lonter.jfa.api.exceptions.ErrorResponseException;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.CustomEmojiManager;
import lonter.jfa.api.requests.ErrorResponse;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.requests.restaction.AuditableRestAction;
import lonter.jfa.api.requests.restaction.CacheRestAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.managers.CustomEmojiManagerImpl;
import lonter.jfa.internal.requests.DeferredRestAction;
import lonter.jfa.internal.requests.RestActionImpl;
import lonter.jfa.internal.requests.restaction.AuditableRestActionImpl;
import lonter.jfa.internal.utils.EntityString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

public class RichCustomEmojiImpl implements RichCustomEmoji, EmojiUnion {
    private final long id;
    private final JFAImpl api;
    private final Set<Role> roles;

    private GuildImpl guild;
    private boolean managed = false;
    private boolean available = true;
    private boolean animated = false;
    private String name;
    private User owner;

    public RichCustomEmojiImpl(long id, GuildImpl guild) {
        this.id = id;
        this.api = guild.getJFA();
        this.guild = guild;
        this.roles = ConcurrentHashMap.newKeySet();
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.CUSTOM;
    }

    @NotNull
    @Override
    public String getAsReactionCode() {
        return name + ":" + id;
    }

    @NotNull
    @Override
    public DataObject toData() {
        return DataObject.empty().put("name", name).put("animated", animated).put("id", id);
    }

    @NotNull
    @Override
    public GuildImpl getGuild() {
        GuildImpl realGuild = (GuildImpl) api.getGuildById(guild.getIdLong());
        if (realGuild != null) {
            guild = realGuild;
        }
        return guild;
    }

    @NotNull
    @Override
    public List<Role> getRoles() {
        return Collections.unmodifiableList(new ArrayList<>(roles));
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @NotNull
    @Override
    public JFAImpl getJFA() {
        return api;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public CacheRestAction<User> retrieveOwner() {
        GuildImpl guild = getGuild();
        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(guild, Permission.MANAGE_GUILD_EXPRESSIONS);
        }
        return new DeferredRestAction<>(api, User.class, this::getOwner, () -> {
            Route.CompiledRoute route = Route.Emojis.GET_EMOJI.compile(guild.getId(), getId());
            return new RestActionImpl<>(api, route, (response, request) -> {
                DataObject data = response.getObject();
                if (data.isNull("user")) { // user is not provided when permissions are missing
                    throw ErrorResponseException.create(ErrorResponse.MISSING_PERMISSIONS, response);
                }
                DataObject user = data.getObject("user");
                return this.owner = api.getEntityBuilder().createUser(user);
            });
        });
    }

    @NotNull
    @Override
    public CustomEmojiManager getManager() {
        return new CustomEmojiManagerImpl(this);
    }

    @Override
    public boolean isAnimated() {
        return animated;
    }

    @NotNull
    @Override
    public AuditableRestAction<Void> delete() {
        if (managed) {
            throw new UnsupportedOperationException("You cannot delete a managed emoji!");
        }
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_GUILD_EXPRESSIONS);
        }

        Route.CompiledRoute route = Route.Emojis.DELETE_EMOJI.compile(getGuild().getId(), getId());
        return new AuditableRestActionImpl<>(getJFA(), route);
    }

    // -- Setters --

    public RichCustomEmojiImpl setName(String name) {
        this.name = name;
        return this;
    }

    public RichCustomEmojiImpl setAnimated(boolean animated) {
        this.animated = animated;
        return this;
    }

    public RichCustomEmojiImpl setManaged(boolean val) {
        this.managed = val;
        return this;
    }

    public RichCustomEmojiImpl setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public RichCustomEmojiImpl setOwner(User user) {
        this.owner = user;
        return this;
    }

    // -- Set Getter --

    public Set<Role> getRoleSet() {
        return this.roles;
    }

    // -- Object overrides --

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CustomEmoji)) {
            return false;
        }

        CustomEmoji other = (CustomEmoji) obj;
        return this.id == other.getIdLong();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return new EntityString(this).setName(name).toString();
    }

    public RichCustomEmojiImpl copy() {
        RichCustomEmojiImpl copy = new RichCustomEmojiImpl(id, getGuild())
                .setOwner(owner)
                .setManaged(managed)
                .setAnimated(animated)
                .setName(name);
        copy.roles.addAll(roles);
        return copy;
    }

    @NotNull
    @Override
    public UnicodeEmoji asUnicode() {
        throw new IllegalStateException("Cannot convert CustomEmoji to UnicodeEmoji!");
    }

    @NotNull
    @Override
    public CustomEmoji asCustom() {
        return this;
    }

    @NotNull
    @Override
    public RichCustomEmoji asRich() {
        return this;
    }

    @NotNull
    @Override
    public ApplicationEmoji asApplication() {
        throw new IllegalStateException("Cannot convert RichCustomEmoji to ApplicationEmoji!");
    }
}
