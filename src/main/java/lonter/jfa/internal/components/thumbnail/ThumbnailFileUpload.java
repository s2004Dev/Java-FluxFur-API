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

package lonter.jfa.internal.components.thumbnail;

import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.components.section.SectionAccessoryComponentUnion;
import lonter.jfa.api.components.thumbnail.Thumbnail;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
import lonter.jfa.internal.entities.FileContainerMixin;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.EntityString;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThumbnailFileUpload extends AbstractComponentImpl
        implements Thumbnail, SectionAccessoryComponentUnion, FileContainerMixin {
    private final int uniqueId;
    private final FileUpload file;
    private final String description;
    private final boolean spoiler;

    public ThumbnailFileUpload(FileUpload file) {
        this(-1, file, null, false);
    }

    public ThumbnailFileUpload(int uniqueId, FileUpload file, String description, boolean spoiler) {
        this.uniqueId = uniqueId;
        this.file = file;
        this.description = description;
        this.spoiler = spoiler;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.THUMBNAIL;
    }

    @NotNull
    @Override
    public ThumbnailFileUpload withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
    }

    @NotNull
    @Override
    public Thumbnail withDescription(@Nullable String description) {
        if (description != null) {
            Checks.notBlank(description, "Description");
            Checks.notLonger(description, MAX_DESCRIPTION_LENGTH, "Description");
        }
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
    }

    @NotNull
    @Override
    public Thumbnail withSpoiler(boolean spoiler) {
        return new ThumbnailFileUpload(uniqueId, file, description, spoiler);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @NotNull
    @Override
    public String getUrl() {
        return "attachment://" + file.getName();
    }

    @Override
    public Stream<FileUpload> getFiles() {
        return Stream.of(file);
    }

    @Nullable
    @Override
    public ResolvedMedia getResolvedMedia() {
        return null;
    }

    @Nullable
    @Override
    public String getDescription() {
        if (description != null) {
            return description;
        }
        return file.getDescription(); // FileUpload is mutable
    }

    @Override
    public boolean isSpoiler() {
        return spoiler;
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                .put("media", DataObject.empty().put("url", getUrl()))
                .put("spoiler", spoiler);
        if (uniqueId >= 0) {
            json.put("id", uniqueId);
        }
        if (getDescription() != null) {
            json.put("description", getDescription());
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ThumbnailFileUpload)) {
            return false;
        }
        ThumbnailFileUpload thumbnail = (ThumbnailFileUpload) o;
        return uniqueId == thumbnail.uniqueId
                && spoiler == thumbnail.spoiler
                && Objects.equals(file, thumbnail.file)
                && Objects.equals(description, thumbnail.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, file, description, spoiler);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("file", file)
                .addMetadata("spoiler", spoiler)
                .addMetadata("description", description)
                .toString();
    }
}
