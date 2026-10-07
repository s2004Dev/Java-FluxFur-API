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
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.sticker.StickerSnowflake;
import lonter.jfa.api.exceptions.InsufficientPermissionException;
import lonter.jfa.api.managers.GuildStickerManager;
import lonter.jfa.api.requests.Route;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.utils.Checks;
import okhttp3.RequestBody;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildStickerManagerImpl extends ManagerBase<GuildStickerManager> implements GuildStickerManager {
    private final Guild guild;
    private final long guildId;
    private String name;
    private String description;
    private String tags;

    public GuildStickerManagerImpl(Guild guild, long guildId, StickerSnowflake sticker) {
        super(
                guild.getJFA(),
                Route.Stickers.MODIFY_GUILD_STICKER.compile(Long.toUnsignedString(guildId), sticker.getId()));
        this.guild = guild;
        this.guildId = guildId;
        if (isPermissionChecksEnabled()) {
            checkPermissions();
        }
    }

    @Nullable
    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public long getGuildIdLong() {
        return guildId;
    }

    @NotNull
    @Override
    public GuildStickerManagerImpl reset(long fields) {
        super.reset(fields);
        if ((fields & NAME) == NAME) {
            this.name = null;
        }
        if ((fields & DESCRIPTION) == DESCRIPTION) {
            this.description = null;
        }
        if ((fields & TAGS) == TAGS) {
            this.tags = null;
        }
        return this;
    }

    @NotNull
    @Override
    public GuildStickerManagerImpl reset(@NotNull long... fields) {
        super.reset(fields);
        return this;
    }

    @NotNull
    @Override
    public GuildStickerManagerImpl reset() {
        super.reset();
        this.name = null;
        this.description = null;
        this.tags = null;
        return this;
    }

    @NotNull
    @Override
    public GuildStickerManager setName(@NotNull String name) {
        Checks.inRange(name, 2, 30, "Name");
        this.name = name;
        set |= NAME;
        return this;
    }

    @NotNull
    @Override
    public GuildStickerManager setDescription(@NotNull String description) {
        Checks.inRange(description, 2, 100, "Description");
        this.description = description;
        set |= DESCRIPTION;
        return this;
    }

    @NotNull
    @Override
    public GuildStickerManager setTags(@NotNull Collection<String> tags) {
        Checks.notEmpty(tags, "Tags");
        for (String tag : tags) {
            Checks.notEmpty(tag, "Tags"); // checks for empty and null
        }
        String csv = String.join(",", tags);
        Checks.notLonger(csv, 200, "List of tags");
        this.tags = csv;
        set |= TAGS;
        return this;
    }

    @Override
    protected RequestBody finalizeData() {
        DataObject object = DataObject.empty();
        if (shouldUpdate(NAME)) {
            object.put("name", name);
        }
        if (shouldUpdate(DESCRIPTION)) {
            object.put("description", description);
        }
        if (shouldUpdate(TAGS)) {
            object.put("tags", tags);
        }
        reset();
        return getRequestBody(object);
    }

    @Override
    protected boolean checkPermissions() {
        if (guild != null && !guild.getSelfMember().hasPermission(Permission.MANAGE_GUILD_EXPRESSIONS)) {
            throw new InsufficientPermissionException(guild, Permission.MANAGE_GUILD_EXPRESSIONS);
        }
        return super.checkPermissions();
    }
}
