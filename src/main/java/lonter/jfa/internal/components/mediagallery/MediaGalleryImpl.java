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

package lonter.jfa.internal.components.mediagallery;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.container.ContainerChildComponentUnion;
import lonter.jfa.api.components.mediagallery.MediaGallery;
import lonter.jfa.api.components.mediagallery.MediaGalleryItem;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataArray;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.entities.FileContainerMixin;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;
import lonter.jfa.internal.utils.Helpers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class MediaGalleryImpl extends AbstractComponentImpl
        implements MediaGallery, MessageTopLevelComponentUnion, ContainerChildComponentUnion, FileContainerMixin {
    private final int uniqueId;
    private final List<MediaGalleryItem> items;

    private MediaGalleryImpl(Collection<? extends MediaGalleryItem> items) {
        this(-1, items);
    }

    public MediaGalleryImpl(int uniqueId, Collection<? extends MediaGalleryItem> items) {
        this.uniqueId = uniqueId;
        this.items = Helpers.copyAsUnmodifiableList(items);
    }

    @NotNull
    public static MediaGallery validated(@NotNull Collection<? extends MediaGalleryItem> items) {
        Checks.noneNull(items, "Items");
        Checks.notEmpty(items, "Items");
        Checks.check(
                items.size() <= MAX_ITEMS,
                "A media gallery can only contain %d items, provided: %d",
                MAX_ITEMS,
                items.size());
        return new MediaGalleryImpl(items);
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.MEDIA_GALLERY;
    }

    @NotNull
    @Override
    public MediaGalleryImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new MediaGalleryImpl(uniqueId, items);
    }

    @NotNull
    @Override
    public MediaGalleryImpl withItems(@NotNull Collection<? extends MediaGalleryItem> items) {
        Checks.noneNull(items, "Items");
        return new MediaGalleryImpl(uniqueId, items);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @NotNull
    @Override
    public List<MediaGalleryItem> getItems() {
        return items;
    }

    @Override
    public Stream<FileUpload> getFiles() {
        return items.stream()
                .filter(FileContainerMixin.class::isInstance)
                .map(FileContainerMixin.class::cast)
                .flatMap(FileContainerMixin::getFiles);
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json =
                DataObject.empty().put("type", getType().getKey()).put("items", DataArray.fromCollection(getItems()));
        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MediaGalleryImpl)) {
            return false;
        }
        MediaGalleryImpl that = (MediaGalleryImpl) o;
        return uniqueId == that.uniqueId && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, items);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("items", items)
                .toString();
    }
}
