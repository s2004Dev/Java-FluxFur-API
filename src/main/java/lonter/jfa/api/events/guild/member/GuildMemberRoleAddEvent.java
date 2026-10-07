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

package lonter.jfa.api.events.guild.member;

import lonter.jfa.api.JFA;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.Role;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that one or more {@link lonter.jfa.api.entities.Role Roles} were assigned to a {@link lonter.jfa.api.entities.Member Member}.
 *
 * <p>Can be used to retrieve affected member and guild. Provides a list of added roles.
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link lonter.jfa.api.requests.GatewayIntent#GUILD_MEMBERS GUILD_MEMBERS} intent to be enabled.
 * <br>{@link lonter.jfa.api.JFABuilder#createDefault(String) createDefault(String)} and
 * {@link lonter.jfa.api.JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link lonter.jfa.api.utils.MemberCachePolicy MemberCachePolicy}
 * to cache the updated members. Fluxer does not specifically tell us about the role updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class GuildMemberRoleAddEvent extends GenericGuildMemberEvent {
    private final List<Role> addedRoles;

    public GuildMemberRoleAddEvent(
            @NotNull JFA api, long responseNumber, @NotNull Member member, @NotNull List<Role> addedRoles) {
        super(api, responseNumber, member);
        this.addedRoles = Collections.unmodifiableList(addedRoles);
    }

    /**
     * The list of roles that were added
     *
     * @return The list of roles that were added
     */
    @NotNull
    public List<Role> getRoles() {
        return addedRoles;
    }
}
