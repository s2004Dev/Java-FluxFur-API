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

package lonter.jfa.api.events.user.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the Avatar of a {@link lonter.jfa.api.entities.User User} changed.
 *
 * <p>Can be used to retrieve the User who changed their avatar and their previous Avatar ID/URL.
 *
 * <p>Identifier: {@code avatar}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link lonter.jfa.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Fluxer does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateAvatarEvent extends GenericUserUpdateEvent<String> {
    public static final String IDENTIFIER = "avatar";

    public UserUpdateAvatarEvent(
            @NotNull JFA api, long responseNumber, @NotNull User user, @Nullable String oldAvatar) {
        super(api, responseNumber, user, oldAvatar, user.getAvatarId(), IDENTIFIER);
    }

    /**
     * The previous avatar id
     *
     * @return The previous avatar id
     */
    @Nullable
    public String getOldAvatarId() {
        return getOldValue();
    }

    /**
     * The previous avatar url
     *
     * @return The previous avatar url
     */
    @Nullable
    public String getOldAvatarUrl() {
        return previous == null ? null : getOldAvatarUrl(previous.startsWith("a_") ? ImageFormat.GIF : ImageFormat.PNG);
    }

    /**
     * The previous avatar url
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The previous avatar url
     *
     * @see    FluxerAssets#userAvatar(ImageFormat, String, String)
     */
    @Nullable
    public String getOldAvatarUrl(@NotNull ImageFormat format) {
        ImageProxy proxy = getOldAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this user's old avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Fluxer.
     *
     * @return Possibly-null {@link ImageProxy} of this user's old avatar image
     *
     * @see    #getOldAvatarUrl()
     */
    @Nullable
    public ImageProxy getOldAvatar() {
        String oldAvatarUrl = getOldAvatarUrl();
        return oldAvatarUrl == null ? null : new ImageProxy(oldAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this user's old avatar image.
     * <p>
     * <b>Note:</b> the old avatar may not always be downloadable as it might have been removed from Fluxer.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this user's old avatar image
     *
     * @see    #getOldAvatarUrl(ImageFormat)
     * @see    FluxerAssets#userAvatar(ImageFormat, String, String)
     */
    @Nullable
    public ImageProxy getOldAvatar(@NotNull ImageFormat format) {
        return FluxerAssets.userAvatar(format, getUser().getId(), previous);
    }

    /**
     * The new avatar id
     *
     * @return The new avatar id
     */
    @Nullable
    public String getNewAvatarId() {
        return getNewValue();
    }

    /**
     * The url of the new avatar
     *
     * @return The url of the new avatar
     */
    @Nullable
    public String getNewAvatarUrl() {
        return next == null ? null : getNewAvatarUrl(next.startsWith("a_") ? ImageFormat.GIF : ImageFormat.PNG);
    }

    /**
     * The url of the new avatar
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The url of the new avatar
     *
     * @see    FluxerAssets#userAvatar(ImageFormat, String, String)
     */
    @Nullable
    public String getNewAvatarUrl(@NotNull ImageFormat format) {
        ImageProxy proxy = getNewAvatar(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this user's new avatar image.
     *
     * @return Possibly-null {@link ImageProxy} of this user's new avatar image
     *
     * @see    #getNewAvatarUrl()
     */
    @Nullable
    public ImageProxy getNewAvatar() {
        String newAvatarUrl = getNewAvatarUrl();
        return newAvatarUrl == null ? null : new ImageProxy(newAvatarUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this user's new avatar image.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this user's new avatar image
     *
     * @see    #getNewAvatarUrl(ImageFormat)
     * @see    FluxerAssets#userAvatar(ImageFormat, String, String)
     */
    @Nullable
    public ImageProxy getNewAvatar(@NotNull ImageFormat format) {
        return FluxerAssets.userAvatar(format, getUser().getId(), next);
    }
}
