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

package lonter.jfa.api.components.thumbnail;

import lonter.jfa.api.components.ResolvedMedia;
import lonter.jfa.api.components.section.SectionAccessoryComponent;
import lonter.jfa.api.utils.FileUpload;
import lonter.jfa.api.utils.messages.MessageCreateBuilder;
import lonter.jfa.api.utils.messages.MessageRequest;
import lonter.jfa.internal.components.thumbnail.ThumbnailFileUpload;
import lonter.jfa.internal.components.thumbnail.ThumbnailImpl;
import lonter.jfa.internal.utils.Checks;

import java.io.InputStream;

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

    /**
     * Constructs a new {@link Thumbnail} from the given URL.
     *
     * <h4>Re-uploading external images</h4>
     *
     * If you instead want to re-upload the content of the URL,
     * you have two options to reference images to upload:
     *
     * <ol>
     *     <li>Use {@link #fromFile(FileUpload)} instead, it will set a URI similar to {@code attachment://file.ext},
     *         and automatically adds the attachment to the request.
     *         <br>This is the easiest, but it may upload unnecessary files when editing,
     *         as it is not aware if the file already exists on the message,
     *         wasting bandwidth and potentially slowing down response times.
     *     </li>
     *     <li>
     *         Do the same manually, meaning you need to pass the {@code attachment://file.ext} URI,
     *         where {@code file.ext} is the name of the file you will upload, then, on the request, add the file,
     *         using {@link lonter.jfa.api.utils.messages.MessageCreateRequest#addFiles(FileUpload...) MessageCreateRequest#addFiles(FileUpload...)} for example.
     *         <br>This is more tedious but gives you full control over the uploads.
     *     </li>
     * </ol>
     *
     * <p><u>Example</u>
     * {@snippet lang="java":
     * MessageChannel channel; // = reference of a MessageChannel
     * Thumbnail thumbnail = Thumbnail.fromUrl("attachment://cat.png") // we specify this in sendFile as "cat.png"
     *     .setDescription("This is a cute car :3");
     * Section section = Section.of(
     *     thumbnail,
     *     TextDisplay.of("Sample text")
     * );
     * // It's recommended to use a more robust HTTP library instead,
     * // such as Java 11+'s HttpClient, or OkHttp (included with JFA), among many other options.
     * InputStream file = new URL("https://http.cat/500").openStream();
     * channel.sendFiles(FileUpload.fromData(file, "cat.png"))
     *     .setComponents(section)
     *     .useComponentsV2()
     *     .queue();
     * }
     *
     * @param  url
     *         The URL of the thumbnail to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Thumbnail}
     */
    @NotNull
    static Thumbnail fromUrl(@NotNull String url) {
        Checks.notBlank(url, "URL");
        return new ThumbnailImpl(url);
    }

    /**
     * Constructs a new {@link Thumbnail} from the {@link FileUpload}.
     *
     * <p>This method can also be used to upload external resources,
     * such as by using {@link FileUpload#fromData(InputStream, String)},
     * in which case it will re-upload the entire file.
     *
     * <p>This will automatically add the file when building the message;
     * as such, you do not need to add it manually (with {@link MessageCreateBuilder#addFiles(FileUpload...)} for example).
     *
     * <p><u>Example</u>
     * {@snippet lang="java":
     * MessageChannel channel; // = reference of a MessageChannel
     * // It's recommended to use a more robust HTTP library instead,
     * // such as Java 11+'s HttpClient, or OkHttp (included with JFA), among many other options.
     * InputStream file = new URL("https://http.cat/500").openStream();
     * // You can also replace this with a local file
     * Thumbnail thumbnail = Thumbnail.fromFile(FileUpload.fromData(file, "cat.png"))
     *     .setDescription("This is a cute car :3");
     * Section section = Section.of(
     *     thumbnail,
     *     TextDisplay.of("Sample text")
     * );
     * channel.sendComponents(section)
     *     .useComponentsV2()
     *     .queue();
     * }
     *
     * @param  file
     *         The {@link FileUpload} to display
     *
     * @throws IllegalArgumentException
     *         If {@code null} is provided
     *
     * @return The new {@link Thumbnail}
     */
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
