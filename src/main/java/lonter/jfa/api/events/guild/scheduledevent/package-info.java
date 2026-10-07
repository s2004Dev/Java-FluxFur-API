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
 * Gateway events for {@link lonter.jfa.api.entities.ScheduledEvent ScheduledEvents}.
 * <p> It should be noted that {@link lonter.jfa.api.entities.ScheduledEvent ScheduledEvents} are not
 * actual gateway events found in the {@link lonter.jfa.api.events} package, but are rather entities similar to
 * {@link lonter.jfa.api.entities.User User} or {@link lonter.jfa.api.entities.channel.concrete.TextChannel TextChannel} objects
 * representing a <a href="https://support.fluxer.com/hc/en-us/articles/4409494125719-Scheduled-Events">scheduled event</a>.
 */
package lonter.jfa.api.events.guild.scheduledevent;
