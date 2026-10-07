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

/**
 * Events for {@link lonter.jfa.api.entities.ScheduledEvent ScheduleEvent} updates.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>These events require the {@link lonter.jfa.api.requests.GatewayIntent#SCHEDULED_EVENTS SCHEDULED_EVENTS} intent and {@link lonter.jfa.api.utils.cache.CacheFlag#SCHEDULED_EVENTS} to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Fluxer does not specifically tell us about the updates, but merely tells us the
 * {@link lonter.jfa.api.entities.ScheduledEvent ScheduledEvent} was updated and gives us the updated {@link lonter.jfa.api.entities.ScheduledEvent ScheduledEvent} object.
 * In order to fire specific events we need to have the old {@link lonter.jfa.api.entities.ScheduledEvent ScheduledEvent} cached to compare against.
 */
package lonter.jfa.api.events.guild.scheduledevent.update;
