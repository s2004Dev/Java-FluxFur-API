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

package lonter.jfa.internal.handle;

import gnu.trove.map.TLongObjectMap;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.events.sticker.GuildStickerAddedEvent;
import lonter.jfa.api.events.sticker.GuildStickerRemovedEvent;
import lonter.jfa.api.events.sticker.update.GuildStickerUpdateAvailableEvent;
import lonter.jfa.api.events.sticker.update.GuildStickerUpdateDescriptionEvent;
import lonter.jfa.api.events.sticker.update.GuildStickerUpdateNameEvent;
import lonter.jfa.api.events.sticker.update.GuildStickerUpdateTagsEvent;
import lonter.jfa.api.utils.cache.CacheFlag;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.JFAImpl;
import lonter.jfa.internal.entities.EntityBuilder;
import lonter.jfa.internal.entities.GuildImpl;
import lonter.jfa.internal.entities.sticker.GuildStickerImpl;
import lonter.jfa.internal.utils.Helpers;
import lonter.jfa.internal.utils.UnlockHook;
import lonter.jfa.internal.utils.cache.SnowflakeCacheViewImpl;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuildStickersUpdateHandler extends SocketHandler {
    public GuildStickersUpdateHandler(JFAImpl api) {
        super(api);
    }

    @Override
    protected Long handleInternally(DataObject content) {
        if (!getJFA().isCacheFlagSet(CacheFlag.STICKER)) {
            return null;
        }
        long guildId = content.getLong("guild_id");
        if (getJFA().getGuildSetupController().isLocked(guildId)) {
            return guildId;
        }

        GuildImpl guild = (GuildImpl) getJFA().getGuildById(guildId);
        if (guild == null) {
            getJFA().getEventCache().cache(EventCache.Type.GUILD, guildId, responseNumber, allContent, this::handle);
            return null;
        }

        DataArray array = content.getArray("stickers");
        List<GuildSticker> oldStickers, newStickers;
        SnowflakeCacheViewImpl<GuildSticker> stickersView = guild.getStickersView();
        EntityBuilder builder = api.getEntityBuilder();
        try (UnlockHook hook = stickersView.writeLock()) {
            TLongObjectMap<GuildSticker> stickersMap = stickersView.getMap();
            oldStickers = new ArrayList<>(stickersMap.valueCollection()); // snapshot of sticker cache
            newStickers = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                DataObject current = array.getObject(i);
                long stickerId = current.getLong("id");
                GuildStickerImpl sticker = (GuildStickerImpl) stickersMap.get(stickerId);
                GuildStickerImpl oldSticker = null;

                if (sticker == null) {
                    sticker = (GuildStickerImpl) builder.createRichSticker(current);
                    newStickers.add(sticker);
                } else {
                    // sticker is in our cache
                    // which is why we don't want to remove it in cleanup later
                    oldStickers.remove(sticker);
                    oldSticker = sticker.copy();
                }

                sticker.setName(current.getString("name"));
                sticker.setAvailable(current.getBoolean("available"));
                sticker.setDescription(current.getString("description", ""));
                sticker.setTags(Helpers.setOf(current.getString("tags").split(",\\s*")));

                // finally, update the sticker
                stickersMap.put(sticker.getIdLong(), sticker);
                // check for updated fields and fire events
                handleReplace(guild, oldSticker, sticker);
            }
            for (GuildSticker e : oldStickers) {
                stickersMap.remove(e.getIdLong());
            }
        }
        // cleanup old stickers that don't exist anymore
        for (GuildSticker e : oldStickers) {
            getJFA().handleEvent(new GuildStickerRemovedEvent(getJFA(), responseNumber, guild, e));
        }

        for (GuildSticker e : newStickers) {
            getJFA().handleEvent(new GuildStickerAddedEvent(getJFA(), responseNumber, guild, e));
        }

        return null;
    }

    private void handleReplace(Guild guild, GuildStickerImpl oldSticker, GuildStickerImpl newSticker) {
        if (oldSticker == null || newSticker == null) {
            return;
        }

        if (!Objects.equals(oldSticker.getName(), newSticker.getName())) {
            getJFA().handleEvent(new GuildStickerUpdateNameEvent(
                    getJFA(), responseNumber, guild, newSticker, oldSticker.getName()));
        }

        if (!Objects.equals(oldSticker.getDescription(), newSticker.getDescription())) {
            getJFA().handleEvent(new GuildStickerUpdateDescriptionEvent(
                    getJFA(), responseNumber, guild, newSticker, oldSticker.getDescription()));
        }

        if (oldSticker.isAvailable() != newSticker.isAvailable()) {
            getJFA().handleEvent(new GuildStickerUpdateAvailableEvent(
                    getJFA(), responseNumber, guild, newSticker, oldSticker.isAvailable()));
        }

        if (!CollectionUtils.isEqualCollection(oldSticker.getTags(), newSticker.getTags())) {
            getJFA().handleEvent(new GuildStickerUpdateTagsEvent(
                    getJFA(), responseNumber, guild, newSticker, oldSticker.getTags()));
        }
    }
}
