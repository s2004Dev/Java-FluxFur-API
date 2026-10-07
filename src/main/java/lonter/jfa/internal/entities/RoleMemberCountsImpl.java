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

package lonter.jfa.internal.entities;

import gnu.trove.map.TLongIntMap;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.RoleMemberCount;
import lonter.jfa.api.entities.RoleMemberCounts;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

import org.jetbrains.annotations.NotNull;

public class RoleMemberCountsImpl implements RoleMemberCounts {
    private final Guild guild;
    private final TLongIntMap roleMemberCounts;

    public RoleMemberCountsImpl(Guild guild, TLongIntMap roleMemberCounts) {
        this.guild = guild;
        this.roleMemberCounts = roleMemberCounts;
    }

    @Override
    public int get(long roleId) {
        // Default value is 0
        return roleMemberCounts.get(roleId);
    }

    @Override
    public boolean contains(long roleId) {
        return roleMemberCounts.containsKey(roleId);
    }

    @NotNull
    @Override
    @Unmodifiable
    public List<RoleMemberCount> asList() {
        List<RoleMemberCount> map = new ArrayList<>(roleMemberCounts.size());
        roleMemberCounts.forEachEntry((roleId, count) -> {
            map.add(new RoleMemberCountImpl(guild, roleId, count));
            return true;
        });
        return Collections.unmodifiableList(map);
    }
}
