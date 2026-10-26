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

package it.lonter.jfa.api.components.filedisplay;

import it.lonter.jfa.api.components.Component;
import it.lonter.jfa.api.components.MessageTopLevelComponent;
import it.lonter.jfa.api.components.ResolvedMedia;
import it.lonter.jfa.api.components.container.ContainerChildComponent;
import it.lonter.jfa.api.utils.FileUpload;
import it.lonter.jfa.api.utils.messages.MessageRequest;
import it.lonter.jfa.internal.components.filedisplay.FileDisplayFileUpload;
import it.lonter.jfa.internal.components.filedisplay.FileDisplayImpl;
import it.lonter.jfa.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component displaying a file, you can mark it as a spoiler.
 *
 * <p>This will appear as a generic download-able file,
 * meaning that audio files and text files cannot be played/previewed.
 * <br>You can instead use {@linkplain it.lonter.jfa.api.components.mediagallery.MediaGallery media galleries} to display images.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} needs to be enabled!
 */
public interface FileDisplay extends Component, MessageTopLevelComponent, ContainerChildComponent {
    @NotNull
    static FileDisplay fromFile(@NotNull FileUpload file) {
        Checks.notNull(file, "FileUpload");
        return new FileDisplayFileUpload(file);
    }

    @NotNull
    static FileDisplay fromFileName(@NotNull String fileName) {
        Checks.notNull(fileName, "File name");
        return new FileDisplayImpl("attachment://" + fileName);
    }

    @NotNull
    @Override
    @CheckReturnValue
    FileDisplay withUniqueId(int uniqueId);

    /**
     * Creates a new {@link FileDisplay} with the provided spoiler status.
     * <br>Spoilers are hidden until the user clicks on it.
     *
     * @param  spoiler
     *         The new spoiler status
     *
     * @return The new {@link FileDisplay}
     */
    @NotNull
    @CheckReturnValue
    FileDisplay withSpoiler(boolean spoiler);

    /**
     * The URL of this file, this is always where the file originally came from.
     * <br>This can be either {@code attachment://filename.extension} or an actual URL.
     *
     * <p>If you want to download the file, you should use {@link #getResolvedMedia()} then {@link ResolvedMedia#getProxy()},
     * to avoid connecting your bot to unknown servers.
     *
     * @return The URL of this file
     */
    @NotNull
    String getUrl();

    /**
     * The media resolved from this file, this is only available if you receive this component from Fluxer.
     *
     * @return Possibly-null {@link ResolvedMedia}
     */
    @Nullable
    ResolvedMedia getResolvedMedia();

    /**
     * Whether this file is hidden until the user clicks on it.
     *
     * @return {@code true} if this is hidden by default, {@code false} otherwise
     */
    boolean isSpoiler();
}
