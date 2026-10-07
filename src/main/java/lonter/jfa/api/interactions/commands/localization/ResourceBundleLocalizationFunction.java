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

package lonter.jfa.api.interactions.commands.localization;

import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.internal.utils.Checks;

import java.util.*;

import org.jetbrains.annotations.NotNull;

/**
 * A default implementation for {@link LocalizationFunction}
 * <p>
 * This implementation supports Java's {@link ResourceBundle} to be used as a localization source
 *
 * <p>
 * You can look at a complete localization example <a href="https://github.com/fluxer-jfa/JFA/blob/master/src/examples/java/LocalizationExample.java" target="_blank">here</a>
 */
public class ResourceBundleLocalizationFunction implements LocalizationFunction {
    private final Set<Bundle> bundles;

    private ResourceBundleLocalizationFunction(Set<Bundle> bundles) {
        this.bundles = bundles;
    }

    @NotNull
    @Override
    public Map<FluxerLocale, String> apply(@NotNull String localizationKey) {
        Map<FluxerLocale, String> map = new HashMap<>();
        for (Bundle bundle : bundles) {
            ResourceBundle resourceBundle = bundle.resourceBundle;
            if (resourceBundle.containsKey(localizationKey)) {
                map.put(bundle.targetLocale, resourceBundle.getString(localizationKey));
            }
        }

        return map;
    }

    /**
     * Creates an empty {@link ResourceBundleLocalizationFunction} builder and adds the provided bundle and locale.
     * <br>This is the same as using {@code ResourceBundleLocalizationFunction.empty().addBundle(resourceBundle, locale)}
     *
     * <p><b>Example usage:</b>
     * <br>This creates a LocalizationFunction from a French ResourceBundle (MyCommands_fr.properties)
     *
     * {@snippet lang="java":
     *     final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
     *                 .fromBundle(ResourceBundle.getBundle("MyCommands", Locale.FRENCH), FluxerLocale.FRENCH)
     *                 .build();
     * }
     *
     * @param  resourceBundle
     *         The resource bundle to get the localized strings from
     * @param  locale
     *         The locale of the resources
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the resource bundle is null</li>
     *             <li>If the locale is null</li>
     *             <li>If the locale is {@link FluxerLocale#UNKNOWN}</li>
     *         </ul>
     *
     * @return The new builder
     */
    @NotNull
    public static Builder fromBundle(@NotNull ResourceBundle resourceBundle, @NotNull FluxerLocale locale) {
        return new Builder().addBundle(resourceBundle, locale);
    }

    /**
     * Creates a {@link ResourceBundleLocalizationFunction} builder with the provided bundles.
     * <br>This will insert the resource bundles with the specified name, with each specified locale.
     * <br>This is the same as using {@code ResourceBundleLocalizationFunction.empty().addBundles(baseName, locales)}
     *
     * <p><b>Example usage:</b>
     * <br>This creates a LocalizationFunction from 2 resource bundles, one in Spanish (MyCommands_es_ES.properties) and one in French (MyCommands_fr.properties)
     *
     * {@snippet lang="java":
     *     final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
     *                         .fromBundles("MyCommands", FluxerLocale.SPANISH, FluxerLocale.FRENCH)
     *                         .build();
     * }
     *
     * @param  baseName
     *         The base name of the resource bundle, for example, the base name of {@code "MyBundle_fr_FR.properties"} would be {@code "MyBundle"}
     * @param  locales
     *         The locales to get from the resource bundle
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the base name is null</li>
     *             <li>If the locales or one of the locale is null</li>
     *             <li>If one of the locale is {@link FluxerLocale#UNKNOWN}</li>
     *         </ul>
     *
     * @return The new builder
     */
    @NotNull
    public static Builder fromBundles(@NotNull String baseName, @NotNull FluxerLocale... locales) {
        return new Builder().addBundles(baseName, locales);
    }

    /**
     * Creates an empty {@link ResourceBundleLocalizationFunction} builder.
     *
     * @return The empty builder
     */
    @NotNull
    public static Builder empty() {
        return new Builder();
    }

    /**
     * Builder for {@link ResourceBundleLocalizationFunction}
     * <br>Use the factory methods in {@link ResourceBundleLocalizationFunction} to create instances of this builder
     *
     * @see ResourceBundleLocalizationFunction#fromBundle(ResourceBundle, FluxerLocale)
     * @see ResourceBundleLocalizationFunction#fromBundles(String, FluxerLocale...)
     */
    public static class Builder {
        private final Set<Bundle> bundles = new HashSet<>();

        protected Builder() {}

        /**
         * Adds a resource bundle to this builder
         *
         * <p>You can see {@link #fromBundle(ResourceBundle, FluxerLocale)} for an example
         *
         * @param  resourceBundle
         *         The {@link ResourceBundle} to get the localized strings from
         * @param  locale
         *         The {@link FluxerLocale} of the resources
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the resource bundle is null</li>
         *             <li>If the locale is null</li>
         *             <li>If the locale is {@link FluxerLocale#UNKNOWN}</li>
         *         </ul>
         *
         * @return This builder for chaining convenience
         *
         * @see #fromBundle(ResourceBundle, FluxerLocale)
         */
        @NotNull
        public Builder addBundle(@NotNull ResourceBundle resourceBundle, @NotNull FluxerLocale locale) {
            Checks.notNull(resourceBundle, "Resource bundle");
            Checks.notNull(locale, "Locale");
            Checks.check(locale != FluxerLocale.UNKNOWN, "Cannot use UNKNOWN FluxerLocale");

            bundles.add(new Bundle(locale, resourceBundle));
            return this;
        }

        /**
         * Adds a resource bundle to this builder
         * <br>This will insert the resource bundles with the specified name, with each specified locale.
         *
         * <p>You can see {@link #fromBundles(String, FluxerLocale...)} for an example
         *
         * @param  baseName
         *         The base name of the resource bundle, for example, the base name of {@code "MyBundle_fr_FR.properties"} would be {@code "MyBundle"}
         * @param  locales
         *         The locales to get from the resource bundle
         *
         * @throws IllegalArgumentException
         *         <ul>
         *             <li>If the base name is null</li>
         *             <li>If the locales or one of the locale is null</li>
         *             <li>If one of the locale is {@link FluxerLocale#UNKNOWN}</li>
         *         </ul>
         *
         * @return This builder for chaining convenience
         *
         * @see #fromBundles(String, FluxerLocale...)
         */
        @NotNull
        public Builder addBundles(@NotNull String baseName, @NotNull FluxerLocale... locales) {
            Checks.notNull(baseName, "Base name");
            Checks.noneNull(locales, "Locale");

            for (FluxerLocale locale : locales) {
                Checks.check(locale != FluxerLocale.UNKNOWN, "Cannot use UNKNOWN FluxerLocale");

                ResourceBundle resourceBundle =
                        ResourceBundle.getBundle(baseName, Locale.forLanguageTag(locale.getLocale()));
                bundles.add(new Bundle(locale, resourceBundle));
            }
            return this;
        }

        /**
         * Builds the resource bundle localization function.
         *
         * @return The new {@link ResourceBundleLocalizationFunction}
         */
        @NotNull
        public ResourceBundleLocalizationFunction build() {
            return new ResourceBundleLocalizationFunction(bundles);
        }
    }

    private static final class Bundle {
        private final FluxerLocale targetLocale;
        private final ResourceBundle resourceBundle;

        public Bundle(FluxerLocale targetLocale, ResourceBundle resourceBundle) {
            this.targetLocale = targetLocale;
            this.resourceBundle = resourceBundle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Bundle)) {
                return false;
            }

            Bundle bundle = (Bundle) o;

            if (!targetLocale.equals(bundle.targetLocale)) {
                return false;
            }
            return resourceBundle.equals(bundle.resourceBundle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetLocale, resourceBundle);
        }
    }
}
