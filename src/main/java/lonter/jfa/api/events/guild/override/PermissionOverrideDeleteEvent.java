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

package lonter.jfa.api.events.guild.override;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link PermissionOverride} in a {@link IPermissionContainer guild channel} has been deleted.
 *
 * <p>Can be used to retrieve the old override.
 */
public class PermissionOverrideDeleteEvent extends GenericPermissionOverrideEvent {
    public PermissionOverrideDeleteEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull IPermissionContainer channel,
            @NotNull PermissionOverride override) {
        super(api, responseNumber, channel, override);
    }
}
