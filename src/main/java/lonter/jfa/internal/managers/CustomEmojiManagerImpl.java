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

package lonter.jfa.internal.managers;

import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.emoji.RichCustomEmoji;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.CustomEmojiManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.emoji.RichCustomEmojiImpl;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public class CustomEmojiManagerImpl extends ManagerBase<CustomEmojiManager> implements CustomEmojiManager {
    protected final RichCustomEmojiImpl emoji;

    protected final List<String> roles = new ArrayList<>();
    protected String name;

    public CustomEmojiManagerImpl(RichCustomEmojiImpl emoji) {
        super(emoji.getJFA(), Route.Emojis.MODIFY_EMOJI.compile(emoji.getGuild().getId(), emoji.getId()));
        this.emoji = emoji;
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    @NotNull
    @Override
    public RichCustomEmoji getEmoji() {
        return emoji;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CustomEmojiManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & ROLES) == ROLES) {
            withLock(this.roles, List::clear);
        }
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CustomEmojiManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CustomEmojiManagerImpl reset() {
        super.reset();
        withLock(this.roles, List::clear);
        this.name = null;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CustomEmojiManagerImpl setName(@NotNull String name) {
        Checks.notBlank(name, "Name");
        name = name.trim();
        Checks.inRange(name, 2, 32, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CustomEmojiManagerImpl setRoles(Set<Role> roles) {
        if (roles == null) {
            withLock(this.roles, List::clear);
        } else {
            Checks.notNull(roles, "Roles");
            roles.forEach((role) -> {
                Checks.notNull(role, "Roles");
                Checks.check(role.getGuild().equals(getGuild()), "Roles must all be from the same guild");
            });
            withLock(this.roles, (list) -> {
                list.clear();
                roles.stream().map(Role::getId).forEach(list::add);
            });
        }
        set |= ROLES;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        if (shouldUpdate(NAME)) {
            object.put("name", name);
        }
        withLock(this.roles, (list) -> {
            if (shouldUpdate(ROLES)) {
                object.put("roles", DataArray.fromCollection(list));
            }
        });
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions() {
        if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(getGuild(), Permission.MANAGE_GUILD_EXPRESSIONS);
        }
        return super.checkPermissions();
    }
}
