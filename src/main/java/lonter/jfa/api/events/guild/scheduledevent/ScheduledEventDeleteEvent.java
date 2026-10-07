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

package lonter.jfa.api.events.guild.scheduledevent;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.ScheduledEvent;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link ScheduledEvent} object has been deleted.
 *
 * <p>Can be used to detect when a {@link ScheduledEvent} was deleted and retrieve the deleted scheduled event.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 */
public class ScheduledEventDeleteEvent extends GenericScheduledEventGatewayEvent {
    public ScheduledEventDeleteEvent(@NotNull JFA api, long responseNumber, @NotNull ScheduledEvent scheduledEvent) {
        super(api, responseNumber, scheduledEvent);
    }
}
