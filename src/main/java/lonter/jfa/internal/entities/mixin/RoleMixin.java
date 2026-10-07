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

package lonter.jfa.internal.entities.mixin;

import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Role;
import lonter.jfa.api.entities.RoleIcon;
import lonter.jfa.api.requests.restaction.RoleAction;
import lonter.jfa.api.utils.data.DataObject;
import lonter.jfa.internal.entities.detached.mixin.IDetachableEntityMixin;
import lonter.jfa.internal.utils.Checks;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

public interface RoleMixin<T extends RoleMixin<T>> extends Role, IDetachableEntityMixin {
    @NotNull
    @Override
    default RoleAction createCopy(@NotNull Guild guild) {
        Checks.notNull(guild, "Guild");
        return guild.createRole()
                .setColors(getColors())
                .setHoisted(isHoisted())
                .setMentionable(isMentionable())
                .setName(getName())
                .setPermissions(getPermissionsRaw())
                .setIcon(
                        // we can only copy the emoji as we don't have access to the Icon instance
                        getIcon() == null ? null : getIcon().getEmoji());
    }

    @Override
    default int compareTo(@NotNull Role r) {
        if (this == r) {
            return 0;
        }

        if (this.getGuild().getIdLong() != r.getGuild().getIdLong()) {
            throw new IllegalArgumentException("Cannot compare roles that aren't from the same guild!");
        }

        if (this.getPositionRaw() != r.getPositionRaw()) {
            return this.getPositionRaw() - r.getPositionRaw();
        }

        OffsetDateTime thisTime = this.getTimeCreated();
        OffsetDateTime rTime = r.getTimeCreated();

        // We compare the provided role's time to this's time
        // instead of the reverse as one would expect due to how fluxer deals with hierarchy.
        // The more recent a role was created,
        // the lower its hierarchy ranking when it shares the same position as another role.
        return rTime.compareTo(thisTime);
    }

    T setName(String name);

    T setPrimaryColor(int color);

    T setSecondaryColor(int color);

    T setTertiaryColor(int color);

    T setManaged(boolean managed);

    T setHoisted(boolean hoisted);

    T setMentionable(boolean mentionable);

    T setRawPermissions(long rawPermissions);

    T setRawPosition(int rawPosition);

    T setTags(DataObject tags);

    T setIcon(RoleIcon icon);
}
