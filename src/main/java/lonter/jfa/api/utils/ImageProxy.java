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

package lonter.jfa.api.utils;

import lonter.jfa.api.entities.Icon;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.FutureUtil;
import lonter.jfa.internal.utils.IOUtil;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class to retrieve images.
 * <br>This supports downloading the images from the normal URL, as well as downloading the image with a specific size.
 *
 * @see <a href="https://fluxer.com/developers/docs/reference#image-formatting" target="_blank">Fluxer docs on image formatting</a>
 */
public class ImageProxy extends FileProxy {
    /**
     * Constructs a new {@link ImageProxy} for the provided URL.
     *
     * @param  url
     *         The URL to download the image from
     *
     * @throws IllegalArgumentException
     *         If the provided URL is null
     */
    public ImageProxy(@NotNull String url) {
        super(url);
    }

    @NotNull
    @Override
    public ImageProxy withClient(@NotNull OkHttpClient customHttpClient) {
        return (ImageProxy) super.withClient(customHttpClient);
    }

    /**
     * Returns the image URL for the specified size.
     * <br>The size is a best-effort resize from Fluxer, with recommended size values as powers of 2 such as 1024 or 512.
     *
     * @param  size
     *         The size of the image
     *
     * @throws IllegalArgumentException
     *         If the requested size is negative or 0
     *
     * @return URL of the image with the specified size
     */
    @NotNull
    public String getUrl(int size) {
        Checks.positive(size, "Image size");

        return IOUtil.addQuery(getUrl(), "size", size);
    }

    /**
     * Retrieves the {@link InputStream} of this image at the specified size.
     * <br><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>,
     * so numbers like 128, 256, 512..., 100 and 600 might also be valid sizes.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * @param  size
     *         The size of this image
     *
     * @return {@link CompletableFuture} which holds an {@link InputStream}, the {@link InputStream} must be closed manually.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<InputStream> download(int size) {
        return download(getUrl(size));
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in a file with the same name
     * as the queried file name (this would be the last segment of the URL).
     * <br><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>,
     * so numbers like 128, 256, 512..., 100 and 600 might also be valid sizes.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  size
     *         The size of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The requested size is negative or 0</li>
     *             <li>The URL's scheme is neither http or https</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link Path} which corresponds to the location the file has been downloaded.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<Path> downloadToPath(int size) {
        return downloadToPath(getUrl(size));
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>,
     * so numbers like 128, 256, 512..., 100 and 600 might also be valid sizes.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *
     * @param  file
     *         The file in which to download the image
     * @param  size
     *         The size of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The target file is null</li>
     *             <li>The parent folder of the target file does not exist</li>
     *             <li>The target file exists and is not a {@link Files#isRegularFile(Path, LinkOption...) regular file}</li>
     *             <li>The target file exists and is not {@link Files#isWritable(Path) writable}</li>
     *             <li>The requested size is negative or 0</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link File}, it is the same as the file passed in the parameters.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<File> downloadToFile(@NotNull File file, int size) {
        Checks.notNull(file, "File");

        CompletableFuture<Path> downloadToPathFuture = downloadToPath(getUrl(size), file.toPath());
        return FutureUtil.thenApplyCancellable(downloadToPathFuture, Path::toFile);
    }

    /**
     * Downloads the data of this image, at the specified size, and stores it in the specified file.
     * <br><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>,
     * so numbers like 128, 256, 512..., 100 and 600 might also be valid sizes.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * <p><b>Implementation note:</b>
     *       The file is first downloaded into a temporary file, the file is then moved to its real destination when the download is complete.
     *       <br>The given path can also target filesystems such as a ZIP filesystem.
     *
     * @param  path
     *         The file in which to download the image
     * @param  size
     *         The size of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If any of the follow checks are true
     *         <ul>
     *             <li>The target path is null</li>
     *             <li>The parent folder of the target path does not exist</li>
     *             <li>The target path exists and is not a {@link Files#isRegularFile(Path, LinkOption...) regular file}</li>
     *             <li>The target path exists and is not {@link Files#isWritable(Path) writable}</li>
     *             <li>The requested size is negative or 0</li>
     *         </ul>
     *
     * @return {@link CompletableFuture} which holds a {@link Path}, it is the same as the path passed in the parameters.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<Path> downloadToPath(@NotNull Path path, int size) {
        Checks.notNull(path, "Path");

        return downloadToPath(getUrl(size), path);
    }

    /**
     * Downloads the data of this attachment, and constructs an {@link Icon} from the data.
     *
     * @return {@link CompletableFuture} which holds an {@link Icon}.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<Icon> downloadAsIcon() {
        return downloadAsIcon(getUrl());
    }

    /**
     * Downloads the data of this image, at the specified size, and constructs an {@link Icon} from the data.
     * <br><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>,
     * so numbers like 128, 256, 512..., 100 and 600 might also be valid sizes.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * @param  size
     *         The size of this image, must be positive
     *
     * @throws IllegalArgumentException
     *         If the requested size is negative or 0
     *
     * @return {@link CompletableFuture} which holds an {@link Icon}.
     */
    @NotNull
    @CheckReturnValue
    public CompletableFuture<Icon> downloadAsIcon(int size) {
        return downloadAsIcon(getUrl(size));
    }

    /**
     * Returns a {@link FileUpload} which supplies a data stream of this attachment,
     * with the given file name and at the specified size.
     * <br>The returned {@link FileUpload} can be reused safely, and does not need to be closed.
     *
     * <p><b>The image may not be resized at any size, usually Fluxer only allows for a few powers of 2</b>, so numbers like 128, 256, 512..., 100 might also be a valid size.
     *
     * <p>If the image is not of a valid size, the CompletableFuture will hold an exception since the HTTP request would have returned a 404.
     *
     * @param  name
     *         The name of the to-be-uploaded file
     * @param  size
     *         The size of this image
     *
     * @throws IllegalArgumentException If the file name is null or blank
     *
     * @return {@link FileUpload} from this attachment.
     */
    @NotNull
    public FileUpload downloadAsFileUpload(@NotNull String name, int size) {
        String url = getUrl(size); // So the checks are also done outside the FileUpload
        return FileUpload.fromStreamSupplier(name, () -> {
            // Blocking is fine on the elastic rate limit thread pool
            // [[JFABuilder#setRateLimitElastic]]
            return download(url).join();
        });
    }
}
