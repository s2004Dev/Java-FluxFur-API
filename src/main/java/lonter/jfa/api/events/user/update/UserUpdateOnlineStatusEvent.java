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

package lonter.jfa.api.events.user.update;

import lonter.jfa.api.JFA;
import lonter.jfa.api.JFABuilder;
import lonter.jfa.api.OnlineStatus;
import lonter.jfa.api.entities.Guild;
import lonter.jfa.api.entities.Member;
import lonter.jfa.api.entities.User;
import lonter.jfa.api.requests.GatewayIntent;
import lonter.jfa.api.utils.MemberCachePolicy;
import lonter.jfa.api.utils.cache.CacheFlag;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that the {@link OnlineStatus} of a {@link User} changed.
 * <br>As with any presence updates this happened for a {@link Member} in a Guild!
 * <p>Can be used to retrieve the User who changed their status and their previous status.
 *
 * <p>Identifier: {@code status}
 *
 * <p><b>Requirements</b><br>
 *
 * <p>This event requires the {@link GatewayIntent#GUILD_PRESENCES GUILD_PRESENCES} intent and {@link CacheFlag#ONLINE_STATUS} to be enabled.
 * <br>{@link JFABuilder#createDefault(String) createDefault(String)} and
 * {@link JFABuilder#createLight(String) createLight(String)} disable this by default!
 *
 * <p>Additionally, this event requires the {@link MemberCachePolicy}
 * to cache the updated members. Fluxer does not specifically tell us about the updates, but merely tells us the
 * member was updated and gives us the updated member object. In order to fire a specific event like this we
 * need to have the old member cached to compare against.
 */
public class UserUpdateOnlineStatusEvent extends GenericUserUpdateEvent<OnlineStatus>
        implements GenericUserPresenceEvent {
    public static final String IDENTIFIER = "status";

    private final Guild guild;
    private final Member member;

    public UserUpdateOnlineStatusEvent(
            @NotNull JFA api, long responseNumber, @NotNull Member member, @NotNull OnlineStatus oldOnlineStatus) {
        super(api, responseNumber, member.getUser(), oldOnlineStatus, member.getOnlineStatus(), IDENTIFIER);
        this.guild = member.getGuild();
        this.member = member;
    }

    @NotNull
    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public Member getMember() {
        return member;
    }

    /**
     * The old status
     *
     * @return The old status
     */
    @NotNull
    public OnlineStatus getOldOnlineStatus() {
        return getOldValue();
    }

    /**
     * The new status
     *
     * @return The new status
     */
    @NotNull
    public OnlineStatus getNewOnlineStatus() {
        return getNewValue();
    }

    @NotNull
    @Override
    public OnlineStatus getOldValue() {
        return super.getOldValue();
    }

    @NotNull
    @Override
    public OnlineStatus getNewValue() {
        return super.getNewValue();
    }
}
