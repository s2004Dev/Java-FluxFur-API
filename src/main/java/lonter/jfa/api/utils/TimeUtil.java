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

package lonter.jfa.api.utils;

import lonter.jfa.api.entities.ISnowflake;
import lonter.jfa.internal.utils.Checks;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

/**
 * Utility for various time related features of the API.
 */
public class TimeUtil {
    public static final long FLUXER_EPOCH = 1420070400000L;
    public static final long TIMESTAMP_OFFSET = 22;
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

    /**
     * Converts the provided epoch millisecond timestamp to a Fluxer Snowflake.
     * <br>This can be used as a marker/pivot for {@link lonter.jfa.api.entities.MessageHistory MessageHistory} creation.
     *
     * @param  millisTimestamp
     *         The epoch millis to convert
     *
     * @return Shifted epoch millis for Fluxer
     */
    public static long getFluxerTimestamp(long millisTimestamp) {
        return (millisTimestamp - FLUXER_EPOCH) << TIMESTAMP_OFFSET;
    }

    /**
     * Gets the creation-time of a JFA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Fluxers side, not inside JFA.
     *
     * @param  entityId
     *         The id of the JFA entity where the creation-time should be determined for
     *
     * @return The creation time of the JFA entity as OffsetDateTime
     */
    @NotNull
    public static OffsetDateTime getTimeCreated(long entityId) {
        long timestamp = (entityId >>> TIMESTAMP_OFFSET) + FLUXER_EPOCH;
        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmt.setTimeInMillis(timestamp);
        return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
    }

    /**
     * Gets the creation-time of a JFA-entity by doing the reverse snowflake algorithm on its id.
     * This returns the creation-time of the actual entity on Fluxers side, not inside JFA.
     *
     * @param  entity
     *         The JFA entity where the creation-time should be determined for
     *
     * @throws IllegalArgumentException
     *         If the provided entity is {@code null}
     *
     * @return The creation time of the JFA entity as OffsetDateTime
     */
    @NotNull
    public static OffsetDateTime getTimeCreated(@NotNull ISnowflake entity) {
        Checks.notNull(entity, "Entity");
        return getTimeCreated(entity.getIdLong());
    }

    /**
     * Returns a prettier String-representation of a OffsetDateTime object
     *
     * @param  time
     *         The OffsetDateTime object to format
     *
     * @return The String of the formatted OffsetDateTime
     */
    @NotNull
    public static String getDateTimeString(@NotNull OffsetDateTime time) {
        return time.format(dtFormatter);
    }
}
