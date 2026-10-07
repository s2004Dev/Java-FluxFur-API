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

package lonter.jfa.api.entities;

import lonter.jfa.api.JFA;
import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;
import lonter.jfa.api.utils.MiscUtil;
import lonter.jfa.internal.entities.UserSnowflakeImpl;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract user reference by only the user ID.
 *
 * <p>This is used for methods which only need a user ID to function, you cannot use this for getting names or similar.
 * To get information about a user by their ID you can use {@link JFA#retrieveUserById(long)} or {@link JFA#getUserById(long)} instead.
 */
public interface UserSnowflake extends IMentionable // Make this a value type whenever that's finally released!
{
    /**
     * Creates a User instance which only wraps an ID.
     *
     * @param  id
     *         The user id
     *
     * @return A user snowflake instance
     *
     * @see    JFA#retrieveUserById(long)
     */
    @NotNull
    static UserSnowflake fromId(long id) {
        return new UserSnowflakeImpl(id);
    }

    /**
     * Creates a User instance which only wraps an ID.
     *
     * @param  id
     *         The user id
     *
     * @throws IllegalArgumentException
     *         If the provided ID is not a valid snowflake
     *
     * @return A user snowflake instance
     *
     * @see    JFA#retrieveUserById(String)
     */
    @NotNull
    static UserSnowflake fromId(@NotNull String id) {
        return fromId(MiscUtil.parseSnowflake(id));
    }

    /**
     * The Fluxer ID for this user's default avatar image.
     *
     * @return Never-null String containing the user's default avatar id.
     */
    @NotNull
    String getDefaultAvatarId();

    /**
     * The URL for the user's default avatar image.
     *
     * <p>Size parameters are ignored by this endpoint.
     *
     * @return Never-null String containing the user's default avatar url.
     *
     * @see    FluxerAssets#userDefaultAvatar(ImageFormat, String)
     */
    @NotNull
    default String getDefaultAvatarUrl() {
        return getDefaultAvatar().getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this user's default avatar.
     *
     * <p>Size parameters are ignored by this endpoint.
     *
     * @return Never-null {@link ImageProxy} of this user's default avatar
     *
     * @see    #getDefaultAvatarUrl()
     * @see    FluxerAssets#userDefaultAvatar(ImageFormat, String)
     */
    @NotNull
    default ImageProxy getDefaultAvatar() {
        return FluxerAssets.userDefaultAvatar(ImageFormat.PNG, getDefaultAvatarId());
    }
}
