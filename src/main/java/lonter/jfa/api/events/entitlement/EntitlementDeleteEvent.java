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
 * Indicates that an {@link Entitlement Entitlement} was deleted. {@link Entitlement Entitlement} deletions are infrequent and occur strictly when:
 * <ul>
 *     <li>Fluxer issues a refund for a subscription; or</li>
 *     <li>Fluxer removes an {@link Entitlement Entitlement} via internal tooling</li>
 * </ul>
 *
 * @see #getEntitlement()
 * @see EntitlementUpdateEvent
 */
public class EntitlementDeleteEvent extends GenericEntitlementEvent {
    public EntitlementDeleteEvent(@NotNull JFA api, long responseNumber, @NotNull Entitlement entitlement) {
        super(api, responseNumber, entitlement);
    }
}
