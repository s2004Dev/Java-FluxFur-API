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

package lonter.jfa.api.events.entitlement;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Entitlement;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates an {@link Entitlement Entitlement} has renewed for the next billing period.
 * The {@link Entitlement#getTimeEnding() timeEnding} will have an updated value with the new expiration date.
 *
 * <p><b>Notice</b><br>
 * The {@link Entitlement#getTimeEnding() timeEnding} is updated for active subscriptions at the end of every billing period to
 * indicate renewal. When an {@link Entitlement Entitlement} has not been renewed, Fluxer will indicate this by not emitting
 * an {@link EntitlementUpdateEvent} with the new {@link Entitlement#getTimeEnding() timeEnding} date
 *
 * @see #getEntitlement()
 */
public class EntitlementUpdateEvent extends GenericEntitlementEvent {
    public EntitlementUpdateEvent(@NotNull JFA api, long responseNumber, @NotNull Entitlement entitlement) {
        super(api, responseNumber, entitlement);
    }
}
