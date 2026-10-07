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
import lonter.jfa.api.events.guild.GenericGuildEvent;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a gateway event relating to a {@link ScheduledEvent} has been fired.
 *
 * <p> It should be noted that a {@link ScheduledEvent} is not an
 * actual gateway event found in the {@link lonter.jfa.api.events} package, but are rather entities similar to
 * {@link lonter.jfa.api.entities.User User} or {@link lonter.jfa.api.entities.channel.concrete.TextChannel TextChannel} objects
 * representing a <a href="https://support.fluxer.com/hc/en-us/articles/4409494125719-Scheduled-Events">scheduled event</a>.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>This class may be used to check if a gateway event is related to a {@link ScheduledEvent}
 * as all gateway events in the {@link lonter.jfa.api.events.guild.scheduledevent} package extend this class.
 */
public abstract class GenericScheduledEventGatewayEvent extends GenericGuildEvent {
    protected final ScheduledEvent scheduledEvent;

    public GenericScheduledEventGatewayEvent(
            @NotNull JFA api, long responseNumber, @NotNull ScheduledEvent scheduledEvent) {
        super(api, responseNumber, scheduledEvent.getGuild());
        this.scheduledEvent = scheduledEvent;
    }

    /**
     * The {@link ScheduledEvent}
     *
     * @return The Scheduled Event
     */
    @NotNull
    public ScheduledEvent getScheduledEvent() {
        return scheduledEvent;
    }
}
