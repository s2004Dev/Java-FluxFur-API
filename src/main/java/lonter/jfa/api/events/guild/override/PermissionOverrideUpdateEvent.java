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
import lonter.jfa.api.Permission;
import lonter.jfa.api.entities.PermissionOverride;
import lonter.jfa.api.entities.channel.attribute.IPermissionContainer;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that a {@link PermissionOverride} in a {@link IPermissionContainer guild channel} has been updated.
 *
 * <p>Can be used to retrieve the updated override and old {@link #getOldAllow() allow} and {@link #getOldDeny() deny}.
 */
public class PermissionOverrideUpdateEvent extends GenericPermissionOverrideEvent {
    private final long oldAllow, oldDeny;

    public PermissionOverrideUpdateEvent(
            @NotNull JFA api,
            long responseNumber,
            @NotNull IPermissionContainer channel,
            @NotNull PermissionOverride override,
            long oldAllow,
            long oldDeny) {
        super(api, responseNumber, channel, override);
        this.oldAllow = oldAllow;
        this.oldDeny = oldDeny;
    }

    /**
     * The old allowed permissions as a raw bitmask.
     *
     * @return The old allowed permissions
     */
    public long getOldAllowRaw() {
        return oldAllow;
    }

    /**
     * The old denied permissions as a raw bitmask.
     *
     * @return The old denied permissions
     */
    public long getOldDenyRaw() {
        return oldDeny;
    }

    /**
     * The old inherited permissions as a raw bitmask.
     *
     * @return The old inherited permissions
     */
    public long getOldInheritedRaw() {
        return ~(oldAllow | oldDeny);
    }

    /**
     * The old allowed permissions
     *
     * @return The old allowed permissions
     */
    @NotNull
    public EnumSet<Permission> getOldAllow() {
        return Permission.getPermissions(oldAllow);
    }

    /**
     * The old denied permissions
     *
     * @return The old denied permissions
     */
    @NotNull
    public EnumSet<Permission> getOldDeny() {
        return Permission.getPermissions(oldDeny);
    }

    /**
     * The old inherited permissions
     *
     * @return The old inherited permissions
     */
    @NotNull
    public EnumSet<Permission> getOldInherited() {
        return Permission.getPermissions(getOldInheritedRaw());
    }
}
