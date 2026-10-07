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

import lonter.jfa.api.entities.ApplicationTeam;
import lonter.jfa.api.entities.TeamMember;
import lonter.jfa.internal.utils.EntityString;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ApplicationTeamImpl implements ApplicationTeam {
    private final String iconId;
    private final List<TeamMember> members;
    private final long id, ownerId;

    public ApplicationTeamImpl(String iconId, List<TeamMember> members, long id, long ownerId) {
        this.iconId = iconId;
        this.members = Collections.unmodifiableList(members);
        this.id = id;
        this.ownerId = ownerId;
    }

    @Override
    public long getOwnerIdLong() {
        return ownerId;
    }

    @Override
    public String getIconId() {
        return iconId;
    }

    @NotNull
    @Override
    public List<TeamMember> getMembers() {
        return members;
    }

    @Override
    public long getIdLong() {
        return id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ApplicationTeamImpl)) {
            return false;
        }
        ApplicationTeamImpl app = (ApplicationTeamImpl) obj;
        return app.id == this.id;
    }

    @Override
    public String toString() {
        return new EntityString(this).toString();
    }
}
