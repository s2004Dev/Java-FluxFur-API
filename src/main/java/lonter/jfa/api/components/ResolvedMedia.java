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

package lonter.jfa.api.components;

import lonter.jfa.api.utils.AttachmentProxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A media resolved by Fluxer including some metadata,
 * typically comes from {@linkplain lonter.jfa.api.entities.Message.MessageFlag#IS_COMPONENTS_V2 V2 Components}.
 */
public interface ResolvedMedia {
    /**
     * The ID of the attachment represented by this resolved media,
     * may return {@code null} if this media was created from an external link.
     *
     * @return The ID of the attachment, or {@code null}
     */
    @Nullable
    String getAttachmentId();

    /**
     * The ID of the attachment represented by this resolved media,
     * may return {@code null} if this media was created from an external link.
     *
     * @return The ID of the attachment, or {@code null}
     */
    @Nullable
    default Long getAttachmentIdLong() {
        String attachmentId = getAttachmentId();
        return attachmentId != null ? Long.parseUnsignedLong(attachmentId) : null;
    }

    /**
     * The URL of this media, for locally-uploaded files, this will always be a URL from Fluxer's CDN,
     * in other cases it <i>may</i> be an external URL.
     *
     * <p>If you want to download the file, you should use {@link #getProxy()}.
     *
     * @return The URL of this media
     */
    @NotNull
    String getUrl();

    /**
     * The URL of this media, proxied by Fluxer's CDN.
     *
     * <p>This URL may be invalid if the media failed to load.
     *
     * <p>If you want to download the file, you should use {@link #getProxy()}.
     *
     * @return The proxy URL of this media
     */
    @NotNull
    String getProxyUrl();

    /**
     * An {@link AttachmentProxy} for this media.
     * <br>This allows you to easily download the media.
     *
     * <p>This proxy may not be usable if the media failed to load.
     *
     * @return The {@link AttachmentProxy} of this media
     */
    @NotNull
    AttachmentProxy getProxy();

    /**
     * The width of this media, if available, or {@code 0}.
     *
     * <p>This may be {@code 0} if the media failed to load.
     *
     * @return Width of this media, or {@code 0}
     */
    int getWidth();

    /**
     * The height of this media, if available, or {@code 0}.
     *
     * <p>This may be {@code 0} if the media failed to load.
     *
     * @return Height of this media, or {@code 0}
     */
    int getHeight();

    /**
     * The <a href="https://en.wikipedia.org/wiki/Media_type" target="_blank">media type</a>,
     * if available, or {@code null}.
     *
     * <p>This may be absent if the media failed to load.
     *
     * @return The media type, or {@code null}
     */
    @Nullable
    String getContentType();
}
