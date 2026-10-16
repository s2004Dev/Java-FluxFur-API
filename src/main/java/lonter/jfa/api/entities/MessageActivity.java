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

import lonter.jfa.api.utils.FluxerAssets;
import lonter.jfa.api.utils.ImageFormat;
import lonter.jfa.api.utils.ImageProxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a {@link Message} activity.
 *
 * @see Message#getActivity()
 */
public record MessageActivity(ActivityType type, String partyId, Application application) {

    /**
     * The current {@link ActivityType ActivityType}
     *
     * @return the type of the activity, or {@link ActivityType#UNKNOWN UNKNOWN}
     */
    @Override
    @NotNull
    public ActivityType type() {
        return type;
    }

    /**
     * The party id fluxer uses internally, it may be {@code null}.
     *
     * @return Possibly-null party id
     */
    @Override
    @Nullable
    public String partyId() {
        return partyId;
    }

    /**
     * The {@link Application Application} this {@link MessageActivity MessageActivity} may have.
     *
     * @return A possibly-null {@link Application}
     */
    @Override
    @Nullable
    public MessageActivity.Application application() {
        return application;
    }

    /**
     * Represents the {@link Application Application} of a MessageActivity, if it has been set.
     */
    public static class Application implements ISnowflake {
        private final String name;
        private final String description;
        private final String iconId;
        private final String coverId;
        private final long id;

        public Application(String name, String description, String iconId, String coverId, long id) {
            this.name = name;
            this.description = description;
            this.iconId = iconId;
            this.coverId = coverId;
            this.id = id;
        }

        /**
         * The name of this Application.
         *
         * @return the applications name
         */
        @NotNull
        public String getName() {
            return name;
        }

        /**
         * A short description of this Application.
         *
         * @return the applications description
         */
        @NotNull
        public String getDescription() {
            return description;
        }

        /**
         * The icon id of this Application.
         *
         * @return the applications icon id
         */
        @Nullable
        public String getIconId() {
            return iconId;
        }

        /**
         * The url of the icon image for this application.
         *
         * @return the url of the icon
         */
        @Nullable
        public String getIconUrl() {
            return getIconUrl(ImageFormat.PNG);
        }

        /**
         * The url of the icon image for this application.
         *
         * @param format The format in which the image should be
         * @return the url of the icon
         * @throws IllegalArgumentException If the format is {@code null}
         * @see FluxerAssets#applicationIcon(ImageFormat, String, String)
         */
        @Nullable
        public String getIconUrl(@NotNull ImageFormat format) {
            ImageProxy proxy = getIcon(format);
            return proxy == null ? null : proxy.getUrl();
        }

        /**
         * Returns an {@link ImageProxy} for this application's icon.
         *
         * @return Possibly-null {@link ImageProxy} of this application's icon
         * @see #getIconUrl()
         */
        @Nullable
        public ImageProxy getIcon() {
            String iconUrl = getIconUrl();
            return iconUrl == null ? null : new ImageProxy(iconUrl);
        }

        /**
         * Returns an {@link ImageProxy} for this application's icon.
         *
         * @param format The format in which the image should be
         * @return Possibly-null {@link ImageProxy} of this application's icon
         * @throws IllegalArgumentException If the format is {@code null}
         * @see #getIconUrl(ImageFormat)
         * @see FluxerAssets#applicationIcon(ImageFormat, String, String)
         */
        @Nullable
        public ImageProxy getIcon(@NotNull ImageFormat format) {
            return FluxerAssets.applicationIcon(format, getId(), getIconId());
        }

        /**
         * The cover aka splash id of this Application.
         *
         * @return the applications cover image/id
         */
        @Nullable
        public String getCoverId() {
            return coverId;
        }

        /**
         * The url of the cover image for this application.
         *
         * @return the url of the cover/splash
         */
        @Nullable
        public String getCoverUrl() {
            return getCoverUrl(ImageFormat.PNG);
        }

        /**
         * The url of the cover image for this application.
         *
         * @param format The format in which the image should be
         * @return the url of the cover/splash
         * @throws IllegalArgumentException If the format is {@code null}v
         * @see FluxerAssets#applicationCover(ImageFormat, String, String)
         */
        @Nullable
        public String getCoverUrl(@NotNull ImageFormat format) {
            ImageProxy proxy = getCover(format);
            return proxy == null ? null : proxy.getUrl();
        }

        /**
         * Returns an {@link ImageProxy} for this cover's icon.
         *
         * @return Possibly-null {@link ImageProxy} of this cover's icon
         * @see #getCoverUrl()
         */
        @Nullable
        public ImageProxy getCover() {
            String coverUrl = getCoverUrl();
            return coverUrl == null ? null : new ImageProxy(coverUrl);
        }

        /**
         * Returns an {@link ImageProxy} for this cover's icon.
         *
         * @param format The format in which the image should be
         * @return Possibly-null {@link ImageProxy} of this cover's icon
         * @throws IllegalArgumentException If the format is {@code null}
         * @see #getCoverUrl(ImageFormat)
         * @see FluxerAssets#applicationCover(ImageFormat, String, String)
         */
        @Nullable
        public ImageProxy getCover(@NotNull ImageFormat format) {
            return FluxerAssets.applicationCover(format, getId(), getCoverId());
        }

        @Override
        public long getIdLong() {
            return id;
        }
    }

    /**
     * An enum representing {@link MessageActivity MessageActivity} types.
     */
    public enum ActivityType {
        /**
         * The {@link MessageActivity MessageActivity} type used for inviting people to join a game.
         */
        JOIN(1),
        /**
         * The {@link MessageActivity MessageActivity} type used for inviting people to spectate a game.
         */
        SPECTATE(2),
        /**
         * The {@link MessageActivity MessageActivity} type used for inviting people to listen (Spotify) together.
         */
        LISTENING(3),
        /**
         * The {@link MessageActivity MessageActivity} type used for requesting to join a game.
         */
        JOIN_REQUEST(5),
        /**
         * Represents any unknown or unsupported {@link MessageActivity MessageActivity} types.
         */
        UNKNOWN(-1);

        private final int id;

        ActivityType(int id) {
            this.id = id;
        }

        /**
         * The id of this {@link ActivityType ActivityType}.
         *
         * @return the id of the type
         */
        public int getId() {
            return id;
        }

        @NotNull
        public static ActivityType fromId(int id) {
            for (ActivityType activityType : values()) {
                if (activityType.id == id) {
                    return activityType;
                }
            }
            return UNKNOWN;
        }
    }
}
