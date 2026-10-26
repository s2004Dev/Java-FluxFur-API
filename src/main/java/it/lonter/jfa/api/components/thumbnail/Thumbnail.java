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

package it.lonter.jfa.api.components.thumbnail;

import it.lonter.jfa.api.components.ResolvedMedia;
import it.lonter.jfa.api.components.section.SectionAccessoryComponent;
import it.lonter.jfa.api.utils.FileUpload;
import it.lonter.jfa.api.utils.messages.MessageRequest;
import it.lonter.jfa.internal.components.thumbnail.ThumbnailFileUpload;
import it.lonter.jfa.internal.components.thumbnail.ThumbnailImpl;
import it.lonter.jfa.internal.utils.Checks;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component displaying a thumbnail, you can mark it as a spoiler and set a description.
 *
 * <p><b>Requirements:</b> {@linkplain MessageRequest#useComponentsV2() Components V2} to be enabled!
 */
public interface Thumbnail extends SectionAccessoryComponent {
    /**
     * The maximum number of characters a thumbnail's description can have. ({@value})
     */
    int MAX_DESCRIPTION_LENGTH = 1024;

    @NotNull
    static Thumbnail fromUrl(@NotNull String url) {
        Checks.notBlank(url, "URL");
        return new ThumbnailImpl(url);
    }

    @NotNull
    static Thumbnail fromFile(@NotNull FileUpload file) {
        Checks.notNull(file, "FileUpload");
        return new ThumbnailFileUpload(file);
    }

    @NotNull
    @Override
    @CheckReturnValue
    Thumbnail withUniqueId(int uniqueId);

    /**
     * Creates a new {@link Thumbnail} with the provided description.
     * <br>The description is known as an "alternative text",
     * and must not exceed {@value #MAX_DESCRIPTION_LENGTH} characters.
     *
     * @param  description
     *         The new description
     *
     * @throws IllegalArgumentException
     *         If the description is longer than {@value #MAX_DESCRIPTION_LENGTH} characters.
     *
     * @return The new {@link Thumbnail}
     */
    @NotNull
    @CheckReturnValue
    Thumbnail withDescription(@Nullable String description);

    /**
     * Creates a new {@link Thumbnail} with the provided spoiler status.
     * <br>Spoilers are hidden until the user clicks on it.
     *
     * @param  spoiler
     *         The new spoiler status
     *
     * @return The new {@link Thumbnail}
     */
    @NotNull
    @CheckReturnValue
    Thumbnail withSpoiler(boolean spoiler);

    /**
     * The URL of this thumbnail, this is always where the file originally came from.
     * <br>This can be either {@code attachment://filename.extension} or an actual URL.
     *
     * <p>If you want to download the file, you should use {@link #getResolvedMedia()} then {@link ResolvedMedia#getProxy()},
     * to avoid connecting your bot to unknown servers.
     *
     * @return The URL of this thumbnail
     */
    @NotNull
    String getUrl();

    /**
     * The media resolved from this thumbnail, this is only available if you receive this component from Fluxer.
     *
     * @return Possibly-null {@link ResolvedMedia}
     */
    @Nullable
    ResolvedMedia getResolvedMedia();

    /**
     * The description of this thumbnail, or {@code null} if none has been set.
     *
     * @return Possibly-null description
     */
    @Nullable
    String getDescription();

    /**
     * Whether this thumbnail is hidden until the user clicks on it.
     *
     * @return {@code true} if this is hidden by default, {@code false} otherwise
     */
    boolean isSpoiler();
}
