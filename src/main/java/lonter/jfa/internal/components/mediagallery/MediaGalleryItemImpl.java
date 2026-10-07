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

import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.components.mediagallery.MediaGalleryItem;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.components.ResolvedMediaImpl;
import lonter.jfa.internal.components.utils.ComponentsUtil;
import lonter.jfa.internal.entities.FileContainerMixin;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents either an external link, an attachment:// link, or an existing item (which is also a link)
 */
public class MediaGalleryItemImpl implements MediaGalleryItem, FileContainerMixin, SerializableData {
    private final String url, description;
    private final ResolvedMedia media;
    private final boolean spoiler;

    public MediaGalleryItemImpl(DataObject obj) {
        this(
                obj.getObject("media").getString("url"),
                obj.getString("description", null),
                new ResolvedMediaImpl(obj.getObject("media")),
                obj.getBoolean("spoiler", false));
    }

    public MediaGalleryItemImpl(String url) {
        this(url, null, null, false);
    }

    public MediaGalleryItemImpl(String url, String description, ResolvedMedia media, boolean spoiler) {
        this.url = url;
        this.media = media;
        this.description = description;
        this.spoiler = spoiler;
    }

    @NotNull
    @Override
    public MediaGalleryItem withDescription(@Nullable String description) {
        if (description != null) {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        }
        return new MediaGalleryItemImpl(url, description, media, spoiler);
    }

    @NotNull
    @Override
    public MediaGalleryItem withSpoiler(boolean spoiler) {
        return new MediaGalleryItemImpl(url, description, media, spoiler);
    }

    @NotNull
    @Override
    public String getUrl() {
        return url;
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia() {
        return media;
    }

    @Override
    public Stream<FileUpload> getFiles() {
        return ComponentsUtil.getFilesFromMedia(media);
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isSpoiler() {
        return spoiler;
    }

    @NotNull
    @Override
    public DataObject toData() {
        String outputUrl = ComponentsUtil.getMediaUrl(media, url);
        return DataObject.empty()
                .put("media", DataObject.empty().put("url", outputUrl))
                .put("description", description)
                .put("spoiler", spoiler);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MediaGalleryItemImpl)) {
            return false;
        }
        MediaGalleryItemImpl that = (MediaGalleryItemImpl) o;
        return spoiler == that.spoiler
                && Objects.equals(url, that.url)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, description, spoiler);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
