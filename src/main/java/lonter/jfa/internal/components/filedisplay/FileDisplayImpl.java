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

package lonter.jfa.internal.components.filedisplay;

import lonter.jfa.api.components.MessageTopLevelComponentUnion;
import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.components.container.ContainerChildComponentUnion;
import lonter.jfa.api.components.filedisplay.FileDisplay;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.components.AbstractComponentImpl;
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
 * Represents either an attachment:// link, or a deserialized file component
 */
public class FileDisplayImpl extends AbstractComponentImpl
        implements FileDisplay, MessageTopLevelComponentUnion, ContainerChildComponentUnion, FileContainerMixin {
    private final int uniqueId;
    private final String url;
    private final ResolvedMedia media;
    private final boolean spoiler;

    public FileDisplayImpl(DataObject data) {
        this(
                data.getInt("id", -1),
                data.getObject("file").getString("url"),
                new ResolvedMediaImpl(data.getObject("file")),
                data.getBoolean("spoiler", false));
    }

    public FileDisplayImpl(String url) {
        this(-1, url, null, false);
    }

    private FileDisplayImpl(int uniqueId, String url, ResolvedMedia media, boolean spoiler) {
        this.uniqueId = uniqueId;
        this.url = url;
        this.media = media;
        this.spoiler = spoiler;
    }

    @NotNull
    @Override
    public Type getType() {
        return Type.FILE_DISPLAY;
    }

    @NotNull
    @Override
    public FileDisplayImpl withUniqueId(int uniqueId) {
        Checks.positive(uniqueId, "Unique ID");
        return new FileDisplayImpl(uniqueId, url, media, spoiler);
    }

    @NotNull
    @Override
    public FileDisplay withSpoiler(boolean spoiler) {
        return new FileDisplayImpl(uniqueId, url, media, spoiler);
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
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

    @Override
    public boolean isSpoiler() {
        return spoiler;
    }

    @NotNull
    @Override
    public DataObject toData() {
        String outputUrl = ComponentsUtil.getMediaUrl(media, url);
        DataObject json = DataObject.empty()
                .put("type", getType().getKey())
                // File components only support attachment://
                .put("file", DataObject.empty().put("url", outputUrl))
                .put("spoiler", spoiler);
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
        if (!(o instanceof FileDisplayImpl)) {
            return false;
        }
        FileDisplayImpl fileDisplay = (FileDisplayImpl) o;
        return uniqueId == fileDisplay.uniqueId
                && spoiler == fileDisplay.spoiler
                && Objects.equals(url, fileDisplay.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId, url, spoiler);
    }

    @Override
    public String toString() {
        return new EntityString(this)
                .addMetadata("id", uniqueId)
                .addMetadata("url", url)
                .addMetadata("media", media)
                .addMetadata("spoiler", spoiler)
                .toString();
    }
}
