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
import lonter.jfa.api.events.Event;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that an {@link Entitlement Entitlement} was either created, updated, or deleted
 *
 * @see EntitlementCreateEvent
 * @see EntitlementUpdateEvent
 * @see EntitlementDeleteEvent
 */
public abstract class GenericEntitlementEvent extends Event {
    protected final Entitlement entitlement;

    protected GenericEntitlementEvent(@NotNull JFA api, long responseNumber, @NotNull Entitlement entitlement) {
        super(api, responseNumber);
        this.entitlement = entitlement;
    }

    /**
     * The {@link Entitlement Entitlement}
     *
     * @return The {@link Entitlement Entitlement}
     */
    @NotNull
    public Entitlement getEntitlement() {
        return entitlement;
    }
}
