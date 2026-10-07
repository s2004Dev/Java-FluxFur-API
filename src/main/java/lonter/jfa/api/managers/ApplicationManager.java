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

package lonter.jfa.api.managers;

import lonter.jfa.api.JFA;
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.ApplicationInfo;
import lonter.jfa.api.entities.Icon;
import lonter.jfa.api.interactions.IntegrationType;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;

import java.util.*;

import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager providing functionality to update one or more fields for the application associated with the bot.
 *
 * @see JFA#getApplicationManager()
 */
public interface ApplicationManager extends Manager<ApplicationManager> {
    /** Used to set description field */
    long DESCRIPTION = 1;
    /** Used to set icon field */
    long ICON = 1 << 1;
    /** Used to set cover image field */
    long COVER_IMAGE = 1 << 2;
    /** Used to set interaction endpoint url field */
    long INTERACTIONS_ENDPOINT_URL = 1 << 3;
    /** Used to set tags field */
    long TAGS = 1 << 4;
    /** Used to set custom install url field */
    long CUSTOM_INSTALL_URL = 1 << 5;
    /** Used to set install params field */
    long INSTALL_PARAMS = 1 << 6;
    /** Used to set integration types config field */
    long INTEGRATION_TYPES_CONFIG = 1 << 7;

    /**
     * Resets the fields specified by the provided bit-flag pattern.
     * You can specify a combination by using a bitwise OR concat of the flag constants.
     * <br>Example: {@code manager.reset(ApplicationManager.ICON | ApplicationManager.COVER_IMAGE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #COVER_IMAGE}</li>
     *     <li>{@link #INTERACTIONS_ENDPOINT_URL}</li>
     *     <li>{@link #TAGS}</li>
     *     <li>{@link #CUSTOM_INSTALL_URL}</li>
     *     <li>{@link #INSTALL_PARAMS}</li>
     *     <li>{@link #INTEGRATION_TYPES_CONFIG}</li>
     * </ul>
     *
     * @param  fields
     *         Integer value containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    ApplicationManager reset(long fields);

    /**
     * Resets the fields specified by the provided bit-flag patterns.
     * <br>Example: {@code manager.reset(ApplicationManager.ICON, ApplicationManager.COVER_IMAGE);}
     *
     * <p><b>Flag Constants:</b>
     * <ul>
     *     <li>{@link #DESCRIPTION}</li>
     *     <li>{@link #ICON}</li>
     *     <li>{@link #COVER_IMAGE}</li>
     *     <li>{@link #INTERACTIONS_ENDPOINT_URL}</li>
     *     <li>{@link #TAGS}</li>
     *     <li>{@link #CUSTOM_INSTALL_URL}</li>
     *     <li>{@link #INSTALL_PARAMS}</li>
     *     <li>{@link #INTEGRATION_TYPES_CONFIG}</li>
     * </ul>
     *
     * @param  fields
     *         Integer values containing the flags to reset.
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @Override
    @CheckReturnValue
    ApplicationManager reset(@NotNull long... fields);

    /**
     * Sets the description of the application.
     *
     * @param  description
     *         The new description (max {@value ApplicationInfo#MAX_DESCRIPTION_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If null is provided or the description is longer than {@value ApplicationInfo#MAX_DESCRIPTION_LENGTH}
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setDescription(@NotNull String description);

    /**
     * Sets the icon of the application.
     *
     * @param  icon
     *         The new icon
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setIcon(@Nullable Icon icon);

    /**
     * Sets the cover image of the application.
     * <br>This is used for rich presence.
     *
     * @param  coverImage
     *         The new coverImage
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setCoverImage(@Nullable Icon coverImage);

    /**
     * Sets up to {@value ApplicationInfo#MAX_TAGS} unique tags for this application.
     *
     * @param  tags
     *         Up to {@value ApplicationInfo#MAX_TAGS} tags (each max {@value ApplicationInfo#MAX_TAG_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If more than {@value ApplicationInfo#MAX_TAGS} tags are provided,
     *         or if any of the tags is null, blank, empty, or longer than {@value ApplicationInfo#MAX_TAG_LENGTH} characters
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setTags(@NotNull Collection<String> tags);

    /**
     * Sets the interactions endpoint URL for this application.
     *
     * <p><b>CAUTION</b>:This will cause JFA to no longer receive interactions through gateway events.
     *
     * @param  interactionsEndpointUrl
     *         The interactions endpoint URL (max {@value ApplicationInfo#MAX_URL_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided URL is longer than {@value ApplicationInfo#MAX_URL_LENGTH} characters
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setInteractionsEndpointUrl(@Nullable String interactionsEndpointUrl);

    /**
     * Sets the custom install url for this application.
     *
     * @param  customInstallUrl
     *         The custom install URL (max {@value ApplicationInfo#MAX_URL_LENGTH} characters)
     *
     * @throws IllegalArgumentException
     *         If the provided URL is longer than {@value ApplicationInfo#MAX_URL_LENGTH} characters
     *
     * @return ApplicationManager for chaining convenience
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setCustomInstallUrl(@Nullable String customInstallUrl);

    /**
     * Sets the default installation parameters of this application.
     *
     * <p>This is used if the application does not use a {@link #setCustomInstallUrl(String) custom install URL}.
     * Configures which scopes and permissions should be applied to invite URLs of this application.
     *
     * @param  installParams
     *         The default install parameters
     *
     * @return ApplicationManager for chaining convenience
     *
     * @see    IntegrationTypeConfig#of(Collection, Collection)
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setInstallParams(@Nullable IntegrationTypeConfig installParams);

    /**
     * Sets the integration type config of this application.
     * <p>This method accepts a map of the supported integration type to an {@link IntegrationTypeConfig} describing the installation parameters.
     *
     * @param  config
     *         The config used for installing this application as integration.
     *
     * @throws IllegalArgumentException
     *         If the config contains null
     *
     * @return ApplicationManager for chaining convenience
     *
     * @see    IntegrationTypeConfig#of(Collection, Collection)
     */
    @NotNull
    @CheckReturnValue
    ApplicationManager setIntegrationTypeConfig(@Nullable Map<IntegrationType, IntegrationTypeConfig> config);

    /**
     * A config describing how the application is installed.
     */
    class IntegrationTypeConfig implements SerializableData {
        private final Set<String> scopes;
        private final long permissions;

        private IntegrationTypeConfig(Set<String> scopes, long permissions) {
            this.scopes = scopes;
            this.permissions = permissions;
        }

        /**
         * Create a simple integration type config.
         *
         * @param  scopes
         *         The default scopes of the integration
         * @param  permissions
         *         The permissions the bot user should receive
         *
         * @return IntegrationTypeConfig instance
         */
        @NotNull
        public static IntegrationTypeConfig of(
                @Nullable Collection<String> scopes, @Nullable Collection<Permission> permissions) {
            return new IntegrationTypeConfig(
                    scopes == null ? Collections.emptySet() : new LinkedHashSet<>(scopes),
                    permissions == null ? 0L : Permission.getRaw(permissions));
        }

        @NotNull
        @Override
        public DataObject toData() {
            return DataObject.empty().put("scopes", scopes).put("permissions", permissions);
        }
    }
}
