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

package lonter.jfa.internal.utils.localization;

import lonter.jfa.api.interactions.FluxerLocale;
import lonter.jfa.api.interactions.commands.localization.LocalizationMap;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.interactions.command.localization.UnmodifiableLocalizationMap;
import lonter.jfa.internal.utils.Checks;
import lonter.jfa.internal.utils.JFALogger;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class LocalizationUtils {
    public static final Logger LOG = JFALogger.getLog(LocalizationUtils.class);

    @NotNull
    public static Map<FluxerLocale, String> mapFromData(@NotNull DataObject data) {
        Checks.notNull(data, "Data");

        Map<FluxerLocale, String> map = new HashMap<>();

        for (String key : data.keys()) {
            FluxerLocale locale = FluxerLocale.from(key);
            if (locale == FluxerLocale.UNKNOWN) {
                LOG.debug("Fluxer provided an unknown locale, locale tag: {}", key);
                continue;
            }

            map.put(locale, data.getString(key));
        }

        return map;
    }

    @NotNull
    public static Map<FluxerLocale, String> mapFromProperty(
            @NotNull DataObject json, @NotNull String localizationProperty) {
        return json.optObject(localizationProperty)
                .map(LocalizationUtils::mapFromData)
                .orElse(Collections.emptyMap());
    }

    @NotNull
    public static LocalizationMap unmodifiableFromProperty(
            @NotNull DataObject json, @NotNull String localizationProperty) {
        return new UnmodifiableLocalizationMap(mapFromProperty(json, localizationProperty));
    }
}
