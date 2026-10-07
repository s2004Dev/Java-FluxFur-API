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

package lonter.jfa.api.events.guild.scheduledevent.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.ScheduledEvent;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates the {@link ScheduledEvent#getDescription() description} of a {@link ScheduledEvent} has changed.
 *
 * <p>Can be used to detect when the {@link ScheduledEvent} description has changed.
 *
 * <p>Identifier: {@code description}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Fluxer does not specifically tell us about the updates, but merely tells us the
 * {@link ScheduledEvent ScheduledEvent} was updated and gives us the updated {@link ScheduledEvent ScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link ScheduledEvent ScheduledEvent} cached to compare against.
 */
public class ScheduledEventUpdateDescriptionEvent extends GenericScheduledEventUpdateEvent<String> {
    public static final String IDENTIFIER = "description";

    public ScheduledEventUpdateDescriptionEvent(
            @NotNull JFA api, long responseNumber, @NotNull ScheduledEvent scheduledEvent, @Nullable String previous) {
        super(api, responseNumber, scheduledEvent, previous, scheduledEvent.getDescription(), IDENTIFIER);
    }

    /**
     * The old {@link ScheduledEvent#getDescription() description}.
     *
     * @return The old description, or {@code null} if no description was previously set.
     */
    @Nullable
    public String getOldDescription() {
        return getOldValue();
    }

    /**
     * The new {@link ScheduledEvent#getDescription() description}.
     *
     * @return The new description, or {@code null} if the description was removed.
     */
    @Nullable
    public String getNewDescription() {
        return getNewValue();
    }
}
