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

package lonter.jfa.api.events.guild.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that the splash of a {@link lonter.jfa.api.entities.Guild Guild} changed.
 *
 * <p>Can be used to detect when a guild splash changes and retrieve the old one
 *
 * <p>Identifier: {@code splash}
 */
public class GuildUpdateSplashEvent extends GenericGuildUpdateEvent<String> {
    public static final String IDENTIFIER = "splash";

    public GuildUpdateSplashEvent(
            @NotNull JFA api, long responseNumber, @NotNull Guild guild, @Nullable String oldSplashId) {
        super(api, responseNumber, guild, oldSplashId, guild.getSplashId(), IDENTIFIER);
    }

    /**
     * The old splash id
     *
     * @return The old splash id, or null
     */
    @Nullable
    public String getOldSplashId() {
        return getOldValue();
    }

    /**
     * The url of the old splash
     *
     * @return The url of the old splash, or null
     */
    @Nullable
    public String getOldSplashUrl() {
        return previous == null ? null : getOldSplashUrl(ImageFormat.PNG);
    }

    /**
     * The url of the old splash
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The url of the old splash, or null
     *
     * @see    FluxerAssets#guildSplash(ImageFormat, String, String)
     */
    @Nullable
    public String getOldSplashUrl(@NotNull ImageFormat format) {
        ImageProxy proxy = getOldSplash(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this guild's old splash image.
     * <p>
     * <b>Note:</b> the old splash may not always be downloadable as it might have been removed from Fluxer.
     *
     * @return Possibly-null {@link ImageProxy} of this guild's old splash image
     *
     * @see    #getOldSplashUrl()
     */
    @Nullable
    public ImageProxy getOldSplash() {
        String oldSplashUrl = getOldSplashUrl();
        return oldSplashUrl == null ? null : new ImageProxy(oldSplashUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this guild's old splash image.
     * <p>
     * <b>Note:</b> the old splash may not always be downloadable as it might have been removed from Fluxer.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this guild's old splash image
     *
     * @see    #getOldSplashUrl(ImageFormat)
     * @see    FluxerAssets#guildSplash(ImageFormat, String, String)
     */
    @Nullable
    public ImageProxy getOldSplash(@NotNull ImageFormat format) {
        return FluxerAssets.guildSplash(format, guild.getId(), previous);
    }

    /**
     * The new splash id
     *
     * @return The new splash id, or null
     */
    @Nullable
    public String getNewSplashId() {
        return getNewValue();
    }

    /**
     * The url of the new splash
     *
     * @return The url of the new splash, or null
     */
    @Nullable
    public String getNewSplashUrl() {
        return next == null ? null : getNewSplashUrl(ImageFormat.PNG);
    }

    /**
     * The url of the new splash
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return The url of the new splash, or null
     *
     * @see    FluxerAssets#guildSplash(ImageFormat, String, String)
     */
    @Nullable
    public String getNewSplashUrl(@NotNull ImageFormat format) {
        ImageProxy proxy = getNewSplash(format);
        return proxy == null ? null : proxy.getUrl();
    }

    /**
     * Returns an {@link ImageProxy} for this guild's new splash image.
     *
     * @return Possibly-null {@link ImageProxy} of this guild's new splash image
     *
     * @see    #getNewSplashUrl(ImageFormat)
     */
    @Nullable
    public ImageProxy getNewSplash() {
        String newSplashUrl = getNewSplashUrl();
        return newSplashUrl == null ? null : new ImageProxy(newSplashUrl);
    }

    /**
     * Returns an {@link ImageProxy} for this guild's new splash image.
     *
     * @param  format
     *         The format in which the image should be
     *
     * @throws IllegalArgumentException
     *         If the format is {@code null}
     *
     * @return Possibly-null {@link ImageProxy} of this guild's new splash image
     *
     * @see    #getNewSplashUrl(ImageFormat)
     * @see    FluxerAssets#guildSplash(ImageFormat, String, String)
     */
    @Nullable
    public ImageProxy getNewSplash(@NotNull ImageFormat format) {
        return FluxerAssets.guildSplash(format, guild.getId(), next);
    }
}
