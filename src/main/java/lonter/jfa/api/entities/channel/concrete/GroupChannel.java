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

package lonter.jfa.api.entities.channel.concrete;

import lonter.jfa.api.entities.User;
import lonter.jfa.api.entities.channel.middleman.MessageChannel;
import lonter.jfa.api.entities.detached.IDetachableEntity;
import lonter.jfa.api.requests.RestAction;
import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Group DM channel.
 *
 * <p>This is only used for user-installed apps.
 */
public interface GroupChannel extends MessageChannel, IDetachableEntity {
    /**
     * Template for {@link #getIconUrl()}.
     *
     * @deprecated Replaced by {@link FluxerAssets#channelIcon(ImageFormat, String, String)}
     */
    @Deprecated
    String ICON_URL = "https://cdn.fluxerapp.com/channel-icons/%s/%s.png";

    /**
     * The Fluxer hash-id of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon hash-id.
     */
    @Nullable
    String getIconId();

    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @return Possibly-null String containing the group channel's icon URL.
     */
    @Nullable
    default String getIconUrl() {
        String iconId = getIconId();
        return iconId == null ? null : getIconUrl(ImageFormat.PNG);
    }

    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns {@code null}.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null String containing the group channel's icon URL.
     *
     * @see    FluxerAssets#channelIcon(ImageFormat, String, String)
     */
    @Nullable
    default String getIconUrl(@NotNull ImageFormat format) {
        ImageProxy icon = getIcon(format);
        return icon == null ? null : icon.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this group channel's icon.
     *
     * @return Possibly-null {@link ImageProxy} of this group channel's icon
     *
     * @see    #getIconUrl()
     */
    @Nullable
    default ImageProxy getIcon() {
        String iconUrl = getIconUrl();
        return iconUrl == null ? null : new ImageProxy(iconUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this group channel's icon.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this group channel's icon
     *
     * @see    #getIconUrl(ImageFormat)
     * @see    FluxerAssets#channelIcon(ImageFormat, String, String)
     */
    @Nullable
    default ImageProxy getIcon(@NotNull ImageFormat format) {
        return FluxerAssets.channelIcon(format, getId(), getIconId());
    }

    /**
     * Returns the ID of the user which owns this {@link GroupChannel}.
     *
     * @return The ID of the user which owns this {@link GroupChannel}
     */
    long getOwnerIdLong();

    /**
     * Returns the ID of the user which owns this {@link GroupChannel}.
     *
     * @return The ID of the user which owns this {@link GroupChannel}
     */
    @NotNull
    default String getOwnerId() {
        return Long.toUnsignedString(getOwnerIdLong());
    }

    /**
     * Retrieves the {@link User} which owns this {@link GroupChannel}.
     *
     * @return A {@link RestAction} to retrieve the {@link User User} which owns this {@link GroupChannel}.
     */
    @NotNull
    @CheckReturnValue
    default RestAction<User> retrieveOwner() {
        return getJFA().retrieveUserById(getOwnerIdLong());
    }
}
