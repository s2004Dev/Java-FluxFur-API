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

import lonter.jfa.api.entities.sticker.GuildSticker;
import lonter.jfa.api.entities.sticker.RichSticker;
import lonter.jfa.api.entities.sticker.StandardSticker;
import lonter.jfa.api.entities.sticker.StickerUnion;
import lonter.jfa.internal.utils.EntityString;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public abstract class RichStickerImpl extends StickerItemImpl implements RichSticker, StickerUnion {
    protected Set<String> tags;
    protected String description;

    public RichStickerImpl(long id, StickerFormat format, String name, Set<String> tags, String description) {
        super(id, format, name);
        this.tags = Collections.unmodifiableSet(tags);
        this.description = description;
    }

    @NotNull
    @Override
    public StandardSticker asStandardSticker() {
        throw new IllegalStateException("Cannot convert sticker of type " + getType() + " to StandardSticker!");
    }

    @NotNull
    @Override
    public GuildSticker asGuildSticker() {
        throw new IllegalStateException("Cannot convert sticker of type " + getType() + " to GuildSticker!");
    }

    @NotNull
    @Override
    public Set<String> getTags() {
        return tags;
    }

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }

    public RichStickerImpl setTags(Set<String> tags) {
        this.tags = Collections.unmodifiableSet(tags);
        return this;
    }

    public RichStickerImpl setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return new EntityString(this).setType(getType()).setName(name).toString();
    }
}
