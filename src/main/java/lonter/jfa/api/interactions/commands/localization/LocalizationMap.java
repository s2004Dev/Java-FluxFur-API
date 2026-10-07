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
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.api.utils.data.SerializableData;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class which contains a mapping from {@link FluxerLocale} to a translated String, similar to a {@code Map<FluxerLocale, String>}.
 * <br>This is used for command, option, and choice localization.
 */
public class LocalizationMap implements SerializableData {
    public static final Logger LOG = JFALogger.getLog(LocalizationMap.class);

    protected final Map<FluxerLocale, String> map = new HashMap<>();
    private final Consumer<String> checkConsumer;

    public LocalizationMap(@NotNull Consumer<String> checkConsumer) {
        this.checkConsumer = checkConsumer;
    }

    private void putTranslation(FluxerLocale locale, String translation) {
        Checks.check(locale != FluxerLocale.UNKNOWN, "Cannot put an 'UNKNOWN' FluxerLocale");
        this.map.put(locale, translation);
    }

    @NotNull
    @Override
    public DataObject toData() {
        DataObject data = DataObject.empty();
        map.forEach((locale, localizedString) -> data.put(locale.getLocale(), localizedString));
        return data;
    }

    /**
     * Sets the given localized string to be used for the specified locale.
     *
     * @param  locale
     *         The locale on which to apply the localized string
     * @param  localizedString
     *         The localized string to use
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the locale is null</li>
     *             <li>If the localized string is null</li>
     *             <li>If the locale is {@link FluxerLocale#UNKNOWN}</li>
     *             <li>If the localized string does not pass the corresponding attribute check</li>
     *         </ul>
     */
    public void setTranslation(@NotNull FluxerLocale locale, @NotNull String localizedString) {
        Checks.notNull(locale, "Locale");
        Checks.notNull(localizedString, "Localized string");

        checkConsumer.accept(localizedString);
        putTranslation(locale, localizedString);
    }

    /**
     * Adds all the translations from the supplied map into this LocalizationMap.
     *
     * @param  map
     *         The map containing the localized strings
     *
     * @throws IllegalArgumentException
     *         <ul>
     *             <li>If the map is null</li>
     *             <li>If the map contains an {@link FluxerLocale#UNKNOWN} key</li>
     *             <li>If the map contains a localized string which does not pass the corresponding attribute check</li>
     *         </ul>
     */
    public void setTranslations(@NotNull Map<FluxerLocale, String> map) {
        Checks.notNull(map, "Map");

        map.forEach((fluxerLocale, localizedString) -> {
            checkConsumer.accept(localizedString);
            putTranslation(fluxerLocale, localizedString);
        });
    }

    /**
     * Gets the localized string for the specified {@link FluxerLocale}.
     *
     * @param  locale
     *         The locale from which to get the localized string
     *
     * @return Possibly-null localized string
     */
    @Nullable
    public String get(@NotNull FluxerLocale locale) {
        Checks.notNull(locale, "Locale");

        return map.get(locale);
    }

    /**
     * Gets the <b>unmodifiable</b> map representing this LocalizationMap.
     * <br>The changes on this LocalizationMap will be reflected on the returned map.
     *
     * @return The unmodifiable map of this LocalizationMap
     */
    @NotNull
    public Map<FluxerLocale, String> toMap() {
        return Collections.unmodifiableMap(map);
    }
}
