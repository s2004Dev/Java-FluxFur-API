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
import lonter.jfa.api.events.UpdateEvent;
import lonter.jfa.api.events.guild.scheduledevent.GenericScheduledEventGatewayEvent;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic gateway event class representing an update of a {@link ScheduledEvent ScheduledEvent} entity.
 * <br> All events in {@link lonter.jfa.api.events.guild.scheduledevent.update} package extend this event and are fired
 * when a specified field in a {@link ScheduledEvent ScheduledEvent} is updated.
 *
 * <p> It should be noted that {@link ScheduledEvent ScheduledEvents} are not
 * actual gateway events found in the {@link lonter.jfa.api.events} package, but are rather entities similar to
 * {@link lonter.jfa.api.entities.User User} or {@link lonter.jfa.api.entities.channel.concrete.TextChannel TextChannel} objects
 * representing a <a href="https://support.fluxer.com/hc/en-us/articles/4409494125719-Scheduled-Events">scheduled event</a>.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Fluxer does not specifically tell us about the updates, but merely tells us the
 * {@link ScheduledEvent ScheduledEvent} was updated and gives us the updated {@link ScheduledEvent ScheduledEvent} object.
 * In order to fire a specific event like this we need to have the old {@link ScheduledEvent ScheduledEvent} cached to compare against.
 */
public abstract class GenericScheduledEventUpdateEvent<T> extends GenericScheduledEventGatewayEvent
        implements UpdateEvent<ScheduledEvent, T> {
    protected final T previous;
    protected final T next;
    protected final String identifier;

    public GenericScheduledEventUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull ScheduledEvent scheduledEvent,
            @Nullable T previous,
            @Nullable T next,
            @NotNull String identifier) {
        super(api, responseNumber, scheduledEvent);
        this.previous = previous;
        this.next = next;
        this.identifier = identifier;
    }

    @NotNull
    @Override
    public ScheduledEvent getEntity() {
        return getScheduledEvent();
    }

    @NotNull
    @Override
    public String getPropertyIdentifier() {
        return identifier;
    }

    @Nullable
    @Override
    public T getOldValue() {
        return previous;
    }

    @Nullable
    @Override
    public T getNewValue() {
        return next;
    }

    @Override
    public String toString() {
        return "ScheduledEventUpdate[" + getPropertyIdentifier() + "](" + getOldValue() + "->" + getNewValue() + ')';
    }
}
