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

package lonter.jfa.api.events.role.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.RoleColors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that a {@link Role} updated its colors.
 *
 * <p>Can be used to retrieve the old colors.
 *
 * <p>Identifier: {@code colors}
 */
public class RoleUpdateColorsEvent extends GenericRoleUpdateEvent<RoleColors> {
    public static final String IDENTIFIER = "colors";

    public RoleUpdateColorsEvent(
            @NotNull JFA api, long responseNumber, @NotNull Role role, @Nullable RoleColors previous) {
        super(api, responseNumber, role, previous, role.getColors(), IDENTIFIER);
    }

    @NotNull
    @Override
    public RoleColors getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public RoleColors getNewValue() {
        return super.getNewValue();
    }
}
